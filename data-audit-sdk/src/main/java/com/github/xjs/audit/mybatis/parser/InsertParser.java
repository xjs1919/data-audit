package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.db.DataSourceUtil;
import com.github.xjs.audit.mybatis.DBActionTypeEnum;
import com.github.xjs.audit.mybatis.Identifiable;
import com.github.xjs.audit.mybatis.dto.*;
import com.github.xjs.audit.mybatis.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.defaults.DefaultSqlSession;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class InsertParser extends AbstractParser {

	private static final String ID_NAME = "id";
	
	@Override
	public List<ChangeRowData> parseBefore(String commandName, MybatisInvocation mybatisInvocation) throws Throwable {
		MappedStatement mappedStatement = mybatisInvocation.getMappedStatement();
		Object parameterObject = mybatisInvocation.getParameter();
		BoundSql boundSql = mappedStatement.getBoundSql(mybatisInvocation.getParameter());
		String sql = boundSql.getSql();
		SqlParserInfo sqlParserInfo = new SqlParserInfo(sql, DBActionTypeEnum.INSERT);
		DataSource dataSource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
		String schemaName = DataSourceUtil.getSchemaName(sqlParserInfo.getSchemaName(), dataSource);
		// 获取插入的字段列表
		List<Map<String, Object>> insertDataMapList = MybatisUtils.getParameter(mappedStatement, boundSql, parameterObject);
		List<ChangeRowData> changeRowDatas = new ArrayList<>();
		for(Map<String, Object> insertDataMap : insertDataMapList){
			ChangeRowData changeData = buildChangeDataForInsert(insertDataMap);
			changeData.setSchemaName(schemaName);
			changeData.setTableName(sqlParserInfo.getTableName());
			changeData.setChangeColumnMap(changeData.afterToChangeColumnMap());
			changeRowDatas.add(changeData);
		}
		return changeRowDatas;
	}

	private ChangeRowData buildChangeDataForInsert(final Map<String, Object> afterDataMap) {
		List<ChangeColumnData> columnList = dataMapToColumnDataList(afterDataMap);
		ChangeRowData changeData = new ChangeRowData();
		changeData.setAfterColumnList(columnList);
		return changeData;
	}

	/**
	 * 回填自动生成的id信息
	 * @param mybatisInvocation
	 * @param changeRows
	 * */
	@Override
	public List<ChangeRowData> parseAfter(MybatisInvocation mybatisInvocation, List<ChangeRowData> changeRows) throws Exception {
		if(changeRows == null || changeRows.size() <= 0){
			return null;
		}
		Object parameter = mybatisInvocation.getParameter();
		if(changeRows.size() <= 1){
			ChangeRowData changeRow = changeRows.get(0);
			Object entityId = changeRow.getEntityId();
			if (entityId == null && parameter instanceof Identifiable) {
				entityId = ((Identifiable) parameter).getId();
				changeRow.setEntityId(entityId);
				changeRow.getAfterColumnList().add(new ChangeColumnData(ID_NAME, entityId));
				changeRow.getChangeColumnMap().put(ID_NAME, new ChangeData(ID_NAME,null,entityId));
			}
			return changeRows;
		}
		//insertBatch(List<User> users);
		if(parameter instanceof DefaultSqlSession.StrictMap){
			DefaultSqlSession.StrictMap strictMap = (DefaultSqlSession.StrictMap)parameter;
			List list = (List)strictMap.get("list");
			fillId(list, changeRows);
		}else if(parameter instanceof MapperMethod.ParamMap){
			//insertBatch(@Param("users")List<User> users);
			MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)parameter;
			Set<Map.Entry> entrySet = paramMap.entrySet();
			for(Map.Entry entry : entrySet){
				String key = (String)entry.getKey();
				Object value = entry.getValue();
				if(key.startsWith("param")){
					if(value instanceof List){
						List list = (List)value;
						fillId(list, changeRows);
					}
				}
			}
		}
		return changeRows;
	}
	private void fillId(List list, List<ChangeRowData> changeRows){
		if(list.size() != changeRows.size()){
			return;
		}
		for(int i=0; i<list.size(); i++){
			Object param = list.get(i);
			if(!(param instanceof Identifiable)){
				break;
			}
			Identifiable identifiable = (Identifiable)param;
			Object id =	identifiable.getId();
			ChangeRowData rowData = changeRows.get(i);
			if(rowData.getEntityId() == null && id != null){
				rowData.setEntityId(id);
				rowData.getAfterColumnList().add(new ChangeColumnData(ID_NAME, id));
				rowData.getChangeColumnMap().put(ID_NAME, new ChangeData(ID_NAME,null,id));
			}
		}
	}
}
