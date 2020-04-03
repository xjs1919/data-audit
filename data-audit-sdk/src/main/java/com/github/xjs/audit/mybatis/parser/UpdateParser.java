package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.db.DataSourceUtil;
import com.github.xjs.audit.mybatis.DBActionTypeEnum;
import com.github.xjs.audit.mybatis.dto.*;
import com.github.xjs.audit.mybatis.util.*;
import com.github.xjs.audit.util.DateUtil;
import com.github.xjs.audit.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.schema.Column;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UpdateParser extends AbstractParser {
	
	@Override
	public List<ChangeRowData> parseBefore(String commandName, MybatisInvocation mybatisInvocation) throws Throwable {
		MappedStatement mappedStatement = mybatisInvocation.getMappedStatement();
		boolean mapUnderscoreToCamelCase = mappedStatement.getConfiguration().isMapUnderscoreToCamelCase();
		Object updateParameterObject = mybatisInvocation.getParameter();
		BoundSql boundSql = mappedStatement.getBoundSql(mybatisInvocation.getParameter());
        DataSource dataSource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
		String sql = boundSql.getSql();
		List<SqlParserInfo> sqlParserInfoList = getParsedSqlList(sql, dataSource);
		List<ChangeRowData> results = new ArrayList<ChangeRowData>();
		//从参数中获取更新字段列表
		List<Map<String, Object>> afterMapParameters = MybatisUtils.getParameter(mappedStatement, boundSql, updateParameterObject);
		//获取数据库列名字和实际传入的字段名子的对应关系
		Map<String, String>  nameMapping = columnNameToRealNameMapping(mapUnderscoreToCamelCase, sql, boundSql.getParameterMappings());
		boolean isBatch = sqlParserInfoList.size()>1;
		for(int i=0; i<sqlParserInfoList.size(); i++){
			SqlParserInfo sqlParserInfo = sqlParserInfoList.get(i);
			Expression whereExpression = sqlParserInfo.getWhereExpression();
			if(whereExpression == null){
				log.error("更新语句没有where条件！！！");
				continue;
			}
			//获取更新之前的数据
			List<Map<String, Object>> beforeResults = query(mybatisInvocation,boundSql, sqlParserInfo, isBatch?i:-1);
			//获取更新之后的参数中的数据
			Map<String, Object> afterMap1 = afterMapParameters.get(i);
			//处理这种：update tbl set a = a+1 where id = ?，变更后的值不在参数中而是在表达式中
			List<Map<String, Object>> afterMap2 = calcFromExpressions(sqlParserInfo, beforeResults, mapUnderscoreToCamelCase);
			//合并afterMap
			List<Map<String, Object>> afterDataMap = combine(afterMap1, afterMap2);
			//组装变更列表
			List<ChangeRowData> changeRows =  buildChangeDatas(nameMapping, afterDataMap, beforeResults, sqlParserInfo);
			if(changeRows != null && changeRows.size() > 0){
				results.addAll(changeRows);
			}
		}
		return results;
	}

	private Map<String, String> columnNameToRealNameMapping(boolean mapUnderscoreToCamelCase, String originalSql, List<ParameterMapping> paramMappings) {
		if(paramMappings == null || paramMappings.size() <= 0){
			return null;
		}
		Pattern p = Pattern.compile("(?is)([\\w]+)\\s*=\\s*\\?");
		String sqlArr[] = originalSql.split(";");
		List<String> columnNames = new ArrayList<>(10);
		for(String sql : sqlArr){
			if(StringUtil.isEmpty(sql)){
				continue;
			}
			Matcher m = p.matcher(sql);
			while(m.find()){
				String columnName = m.group(1);
				columnNames.add(columnName);
			}
		}
		Map<String, String> mapping = new HashMap<>(columnNames.size());
		for(int i=0; i<columnNames.size(); i++){
			String columnName = columnNames.get(i);
			String propertyName = paramMappings.get(i).getProperty();
			int dotIdx = propertyName.indexOf(".");
			String realName = dotIdx > 0? propertyName.substring(dotIdx+1):propertyName;
			mapping.put(mapUnderscoreToCamelCase?MybatisUtils.mapUnderscoreToCamelCase(columnName):columnName, realName);
		}
		return mapping;
	}

	/**
     * 解析要审计的SQL
     * @param sql sql语句
     * @param dataSource 数据源
     * */
	private List<SqlParserInfo> getParsedSqlList(String sql, DataSource dataSource)throws Exception{
        //可能是批量操作，有多条update语句
	    String[] sqlArr = sql.split(";");
        List<SqlParserInfo> sqlParserInfoList = new ArrayList<>(sqlArr.length);
        for(String subSql : sqlArr){
            if(StringUtil.isEmpty(subSql.trim())){
                continue;
            }
            SqlParserInfo sqlParserInfo = new SqlParserInfo(subSql, DBActionTypeEnum.UPDATE);
            String schemaName = DataSourceUtil.getSchemaName(sqlParserInfo.getSchemaName(), dataSource);
			sqlParserInfo.setSchemaName(schemaName);
            sqlParserInfoList.add(sqlParserInfo);
        }
        return sqlParserInfoList;
    }

	private List<ChangeRowData> buildChangeDatas(final Map<String, String> mapping,final List<Map<String, Object>> afterResults, final List<Map<String, Object>> beforeResults, SqlParserInfo sqlParserInfo ){
		List<ChangeRowData> changeDatas = new ArrayList<>();
		if(beforeResults != null && !beforeResults.isEmpty()){
			for(int i=0;i<beforeResults.size();i++){
				Map<String, Object> beforeDataMap = beforeResults.get(i);
				Map<String, Object> afterDataMap = afterResults.get(i);
				ChangeRowData changeData = buildChangeDataForUpdate(mapping, afterDataMap,beforeDataMap);
				changeData.setSchemaName(sqlParserInfo.getSchemaName());
				changeData.setTableName(sqlParserInfo.getTableName());
				changeDatas.add(changeData);
			}
		}
		return changeDatas;
	}

	private ChangeRowData buildChangeDataForUpdate(final Map<String, String> mapping, final Map<String, Object> afterDataMap, Map<String, Object> beforeDataMap) {
		ChangeRowData changeData = new ChangeRowData();
		List<ChangeColumnData> afterColumnList = new ArrayList<>();
		List<ChangeColumnData> beforeColumnList = new ArrayList<>();
		changeData.setAfterColumnList(afterColumnList);
		changeData.setBeforeColumnList(beforeColumnList);
		if (beforeDataMap == null) {
			return changeData;
		}
		Map<String, ChangeData> changeColumnMap = new HashMap<String, ChangeData>();
		for (Map.Entry<String, Object> beforeEntry : beforeDataMap.entrySet()) {
			String beforeKey = beforeEntry.getKey();
			Object beforeValue = beforeEntry.getValue() ;
			// 保存before
			ChangeColumnData beforeColumn = new ChangeColumnData();
			beforeColumn.setName(beforeKey);
			beforeColumn.setValue(beforeValue);
			beforeColumnList.add(beforeColumn);
			// 保存after
			ChangeColumnData afterColumn = new ChangeColumnData();
			afterColumn.setName(beforeKey);
			if(afterDataMap == null){
				afterColumn.setValue(beforeValue);
			}else{
				//首先从afterMap中查找
				String afterDataName = "";
				if(afterDataMap.containsKey(beforeKey)){
					afterDataName = beforeKey;
				}else{
					afterDataName = mapping.get(beforeKey);
				}
				if(StringUtil.isEmpty(afterDataName)){
					log.error("数据库列名字{}没找到对应的值",beforeKey);
					continue;
				}
				if(afterDataMap.containsKey(afterDataName)){
					Object afterValue = afterDataMap.get(afterDataName);
					afterColumn.setValue(afterValue);
					//保存change
					if(beforeValue != null){
						if(beforeValue instanceof Date){
							beforeValue = DateUtil.format((Date)beforeValue, DateUtil.FORMAT_YMDHMS);
						}
					}
					if(afterValue != null){
						if(afterValue instanceof Date){
							afterValue = DateUtil.format((Date)afterValue, DateUtil.FORMAT_YMDHMS);
						}else if(afterValue instanceof Boolean){
							//before可能是数字 after值是true/false  因为before是从数据库获取的，after是从参数中获取的
							afterValue = (Boolean)afterValue?1:0;
						}
					}
					String beforeValueString = beforeValue==null?"":beforeValue.toString();
					String afterValueString = afterValue == null?"":afterValue.toString();
					if(!beforeValueString.equals(afterValueString)){
						changeColumnMap.put(beforeKey, new ChangeData(beforeKey, beforeValue, afterValue));
					}
				}
			}
		}
		//把after中剩余的也添加上，可能是查询条件
		for(Map.Entry<String, Object> entry : afterDataMap.entrySet()){
			String afterKey = entry.getKey();
			String beforeKey = getBeforeKeyFromMapping(mapping, afterKey);
			String key = beforeKey==null?afterKey:beforeKey;
			String value = entry.getValue()==null?"":entry.getValue().toString();
			if(!beforeDataMap.containsKey(key)){
				changeColumnMap.put(key, new ChangeData(key, value, value));
			}
		}
		//说明只把查询条件放进来了
		if(changeColumnMap.size() <= 1){
			changeColumnMap.clear();
		}
		changeData.setChangeColumnMap(changeColumnMap);
		return changeData;
	}

	private Map<String, ChangeData> afterDataMapToChangeData(Map<String, Object> afterDataMap) {
		Map<String, ChangeData> changeMap = new HashMap<String, ChangeData>();
		for(Map.Entry<String, Object> entry : afterDataMap.entrySet()){
			String key = entry.getKey();
			Object value = entry.getValue();
			changeMap.put(key, new ChangeData(key, "-", value));
		}
		return changeMap;
	}

	/**
	 * 合并两个list
	 * @param map1
	 * @param list2
	 * */
	private List<Map<String, Object>> combine(Map<String, Object> map1, List<Map<String, Object>> list2){
		if(map1 == null || map1.size() <= 0){
			return list2;
		}
		if(list2 == null || list2.size() <= 0){
			List<Map<String, Object>> ret = new ArrayList<>();
			ret.add(map1);
			return ret;
		}
		List<Map<String, Object>> ret = new ArrayList<>();
		for(Map<String, Object> map2 : list2){
			map2.putAll(map1);
		}
		return list2;
	}

	/**
	 *
	 * 从sql语句中解析出变化的数据，比如：
	 * sql = "update product set a=a+1,b=100,c=c-1,d=d*10,e=e/10,f=f%10,g=-1,h=0.5,i=a+b where a=100 and b=200 and c=300";
	 * Expression
	 * a=a+1 Addition(Column LongValue)
	 * b=100 LongValue
	 * c=c-1 Subtraction
	 * d=d*10 Multiplication
	 * e=e/10 Division
	 * f = f%10 Modulo
	 * g = -1 SignedExpresssion
	 * h = 0.5 DoubleValue
	 * i = a+b Addition(Column Cloumn)
	 * @param sqlParserInfo sql
	 * @param beforeResults 原始值
	 * */
	private List<Map<String, Object>> calcFromExpressions(SqlParserInfo sqlParserInfo, List<Map<String, Object>> beforeResults, boolean mapUnderscoreToCamelCase){
		if(beforeResults == null){
			return null;
		}
		List<Map<String, Object>> afterMapList = new ArrayList<>();
		for(Map<String, Object> beforeResult : beforeResults){
			Map<String, Object> changeMap = new HashMap<String, Object>();
			List<Expression> expressions = sqlParserInfo.getExpressions();
			List<Column> columns = sqlParserInfo.getColumns();
			for(int i=0;i<expressions.size();i++){
				Expression expression = expressions.get(i);
				Column column = columns.get(i);
				String colName = mapUnderscoreToCamelCase?MybatisUtils.mapUnderscoreToCamelCase(column.getColumnName()):column.getColumnName();
				if(expression instanceof StringValue ){
					changeMap.put(colName, ((StringValue)expression).getValue());
				}else if( expression instanceof LongValue ){
					changeMap.put(colName, ((LongValue)expression).getValue());
				}else if( expression instanceof DoubleValue ){
					changeMap.put(colName, ((DoubleValue)expression).getValue());
				}else if(expression instanceof DateValue ){
					changeMap.put(colName, ((DateValue)expression).getValue());
				}else if( expression instanceof TimeValue ){
					changeMap.put(colName, ((TimeValue)expression).getValue());
				}else if(expression instanceof TimestampValue ){
					changeMap.put(colName, ((TimestampValue)expression).getValue());
				}else if(expression instanceof HexValue ){
					changeMap.put(colName, ((HexValue)expression).getValue());
				}else  if( expression instanceof NullValue){
					changeMap.put(colName, ((NullValue)expression).toString());
				}else  if( expression instanceof SignedExpression){
					changeMap.put(colName, ((SignedExpression)expression).toString());
				} else if(expression instanceof Addition){
					String ret = calc((Addition)expression, beforeResult, (left, right)->left.add(right).toPlainString(),mapUnderscoreToCamelCase);
					changeMap.put(colName, ret);
				}else if(expression instanceof Subtraction){
					String ret = calc((Subtraction)expression, beforeResult, (left, right)->left.subtract(right).toPlainString(),mapUnderscoreToCamelCase);
					changeMap.put(colName, ret);
				}else if(expression instanceof Multiplication){
					String ret = calc((Multiplication)expression, beforeResult, (left, right)->left.multiply(right).toPlainString(),mapUnderscoreToCamelCase);
					changeMap.put(colName, ret);
				}else if(expression instanceof Division){
					String ret = calc((Division)expression, beforeResult, (left, right)->left.divide(right).toPlainString(),mapUnderscoreToCamelCase);
					changeMap.put(colName, ret);
				}else if(expression instanceof Modulo){
					String ret = calc((Modulo)expression, beforeResult, (left, right)->""+(left.toBigInteger().longValue()%right.toBigInteger().longValue()),mapUnderscoreToCamelCase);
					changeMap.put(colName, ret);
				}
			}
			if(changeMap.size() > 0){
				afterMapList.add(changeMap);
			}
		}
		return afterMapList;
	}

	/**
	 * 表达式求值，目前仅支持+ - * / %
	 * @param binaryExpression 表达式
	 * @param beforeResult 列的原始值
	 * @param operator
	 * @param mapUnderscoreToCamelCase 下划线转驼峰
	 * */
	private String calc(BinaryExpression binaryExpression, Map<String, Object> beforeResult, BiFunction<BigDecimal,BigDecimal,String> operator, boolean mapUnderscoreToCamelCase){
		Expression leftExp = binaryExpression.getLeftExpression();
		Expression rightExp = binaryExpression.getRightExpression();
		Object leftValue = getValue(leftExp, beforeResult, mapUnderscoreToCamelCase);
		Object rightValue = getValue(rightExp, beforeResult, mapUnderscoreToCamelCase);
		if(leftValue == null || rightValue == null){
			return "";
		}
		BigDecimal leftBd = new BigDecimal(leftValue.toString());
		BigDecimal rightBd = new BigDecimal(rightValue.toString());
		return operator.apply(leftBd, rightBd);
	}

	/**
	 * 计算表达式的值，如果是字段，则从查询结果中取，如果是数值类型直接取。
	 * @param expression 表达式
	 * @param beforeResult 旧的值
	 * @param mapUnderscoreToCamelCase 下划线转驼峰
	 * */
	private Object getValue(Expression expression, Map<String, Object> beforeResult, boolean mapUnderscoreToCamelCase){
		//如果是字段名，从查询结果中取值
		if(expression instanceof Column){
			String colName = ((Column)expression).getColumnName();
			String leftColName = mapUnderscoreToCamelCase?MybatisUtils.mapUnderscoreToCamelCase(colName):colName;
			return beforeResult.get(leftColName);
		}else{
			//否则就是数值
			return expression.toString();
		}
	}

	private String getBeforeKeyFromMapping(Map<String, String> mapping, String key) {
		for(Map.Entry<String, String> entry : mapping.entrySet()){
			String beforeKey = entry.getKey();
			String afterKey = entry.getValue();
			if(afterKey.equalsIgnoreCase(key)){
				return beforeKey;
			}
		}
		return null;
	}


}
