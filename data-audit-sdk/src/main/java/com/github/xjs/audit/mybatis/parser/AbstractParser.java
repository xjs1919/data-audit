package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.mybatis.DBActionTypeEnum;
import com.github.xjs.audit.mybatis.dto.ChangeColumnData;
import com.github.xjs.audit.mybatis.dto.ChangeRowData;
import com.github.xjs.audit.mybatis.dto.MybatisInvocation;
import com.github.xjs.audit.mybatis.dto.SqlParserInfo;
import com.github.xjs.audit.mybatis.util.*;
import com.github.xjs.audit.util.ReflectionUtil;
import com.github.xjs.audit.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractParser implements IDataParser {

	/**
	 * 查询
	 * @param mybatisInvocation
	 * @param boundSql
	 * @param sqlParserInfo
	 * @param idx -1说明是单个sql
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> query(MybatisInvocation mybatisInvocation, BoundSql boundSql, SqlParserInfo sqlParserInfo, int idx) throws SQLException {
		Table table = sqlParserInfo.getTable();
		MappedStatement mappedStatement = mybatisInvocation.getMappedStatement();
		Configuration configuration = mappedStatement.getConfiguration();
		boolean mapUnderscoreToCamelCase = configuration.isMapUnderscoreToCamelCase();
		//构造select语句
		List<Column> updateColumns = new ArrayList<>();
		//如果是update 精确查询
		if(sqlParserInfo.getActionType() == DBActionTypeEnum.UPDATE){
			updateColumns.addAll(sqlParserInfo.getColumns());
		}else{
			//如果是delete，查询所有
			Column column = new Column();
			column.setColumnName("*");
			updateColumns.add(column);
		}
		Expression whereExpression = sqlParserInfo.getWhereExpression();
		Select select = JsqlParserUtil.getSelect(table, updateColumns, whereExpression);
		//设置select sql
		MappedStatement selectMappedStatement = MybatisUtils.newHashMapMappedStatement(mappedStatement);
		BoundSql queryBoundSql = selectMappedStatement.getBoundSql(mybatisInvocation.getParameter());
		setQuerySql(queryBoundSql, select.toString());
		//设置where所需要的mapping
		List<ParameterMapping> allMappings = queryBoundSql.getParameterMappings();
		List<String> whereColumns = JsqlParserUtil.getWhereColumn(whereExpression);
		List<ParameterMapping> mappings = findMappings(allMappings, idx, whereColumns);
		setParameterMappings(queryBoundSql, mappings);
		//做查询
		Object queryResultList = mybatisInvocation.getExecutor().query(selectMappedStatement, mybatisInvocation.getParameter(), RowBounds.DEFAULT, null, null, queryBoundSql);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> queryResults = (List<Map<String, Object>>) queryResultList;
		return mapUnderscoreToCamelCase(mapUnderscoreToCamelCase, queryResults);
	}

	@Override
	public List<ChangeRowData> parseAfter(MybatisInvocation mybatisInvocation, List<ChangeRowData> changeRows) throws Exception {
		return changeRows;
	}

	protected List<ChangeColumnData> dataMapToColumnDataList(Map<String, Object> dataMap){
		List<ChangeColumnData> columnList = new ArrayList<>();
		if(dataMap == null || dataMap.size() <= 0){
			return columnList;
		}
		for (Map.Entry<String, Object> dataEntry : dataMap.entrySet()) {
			ChangeColumnData changeColumn = new ChangeColumnData();
			changeColumn.setName(dataEntry.getKey());
			changeColumn.setValue(dataEntry.getValue());
			columnList.add(changeColumn);
		}
		return columnList;
	}

	private List<Map<String, Object>> mapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase, List<Map<String, Object>> queryResults){
		if(!mapUnderscoreToCamelCase){
			return queryResults;
		}
		if(queryResults == null){
			return queryResults;
		}
		ArrayList<Map<String, Object>> ret = new ArrayList<>(queryResults.size());
		for(Map<String, Object> result : queryResults){
			HashMap<String, Object> newMap = new HashMap<>();
			for(Map.Entry<String, Object> entry : result.entrySet()){
				String key = entry.getKey();
				Object value = entry.getValue();
				String newKey = MybatisUtils.mapUnderscoreToCamelCase(key);
				newMap.put(newKey, value);
			}
			ret.add(newMap);
		}
		return ret;
	}

	private List<ParameterMapping> findMappings(List<ParameterMapping> allMappings, int idx, List<String> columns){
		if(allMappings == null || allMappings.size() <= 0){
			return new ArrayList<ParameterMapping>();
		}
		List<ParameterMapping> ret = new ArrayList<ParameterMapping>();
		for(ParameterMapping mapping : allMappings){
			String property = mapping.getProperty();
			if(property == null){
				continue;
			}
			PropertyTokenizer token = new PropertyTokenizer(property);
			String indexedName = token.getIndexedName();
			String children = token.getChildren();
			int propertyIdx = MybatisUtils.getPropertyIndex(indexedName);
			if(propertyIdx >= 0){
				if(StringUtil.isEmpty(children)){
					ret.add(mapping);
				}else{
					//说明只有一条sql
					if(idx == -1){
						if(contains(columns, children)){
							ret.add(mapping);
						}
					}else{
						//说明是多条sql
						if((propertyIdx == idx) && contains(columns, children)){
							ret.add(mapping);
						}
					}
				}
			}else{
				if(contains(columns, property)){
					ret.add(mapping);
				}
			}
		}
		//说明没找到，自定义的参数名跟字段名不一致
		if(ret.size() <= 0){
			//碰一下运气
			int columnSize = columns.size();
			int mappingSize = allMappings.size();
			if(columnSize <= mappingSize){
				//从后面往前截取
				List<ParameterMapping> mappings = new ArrayList<>(columnSize);
				for(int i=0; i<columnSize; i++){
					mappings.add(allMappings.get(mappingSize-1-i));
				}
				return mappings;
			}
			return allMappings;
		}
		return ret;
	}

	private void setQuerySql(BoundSql queryBoundSql, String selectSqlString){
		Field field = ReflectionUtil.findField(BoundSql.class, "sql");
		ReflectionUtil.setField(field, queryBoundSql, selectSqlString);
	}

	private void setParameterMappings(BoundSql queryBoundSql, List<ParameterMapping> mappings  ){
		Field field = ReflectionUtil.findField(BoundSql.class, "parameterMappings");
		ReflectionUtil.setField(field, queryBoundSql, mappings);
	}

	private boolean contains(List<String> columns, String propName){
		for(String c : columns){
			if(c.equalsIgnoreCase(propName)){
				return true;
			}
		}
		return false;
	}

}
