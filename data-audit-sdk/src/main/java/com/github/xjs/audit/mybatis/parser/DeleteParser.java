package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.db.DataSourceUtil;
import com.github.xjs.audit.mybatis.DBActionTypeEnum;
import com.github.xjs.audit.mybatis.dto.*;
import com.github.xjs.audit.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class DeleteParser extends AbstractParser {

	@Override
	public List<ChangeRowData> parseBefore(String commandName, MybatisInvocation mybatisInvocation) throws Throwable {
		MappedStatement mappedStatement = mybatisInvocation.getMappedStatement();
		BoundSql boundSql = mappedStatement.getBoundSql(mybatisInvocation.getParameter());
		String sql = boundSql.getSql();
		SqlParserInfo sqlParserInfo = new SqlParserInfo(sql, DBActionTypeEnum.DELETE);
		DataSource dataSource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
		//判断是否是需要审计的表
		String schemaName = DataSourceUtil.getSchemaName(sqlParserInfo.getSchemaName(), dataSource);
		// 获取要删除的数据
		List<Map<String, Object>> beforeResults = query(mybatisInvocation, boundSql, sqlParserInfo);
		List<ChangeRowData> results = buildChangeDatas(schemaName,beforeResults, sqlParserInfo);
		return results;
	}

	/**
	 * 查询
	 * @param mybatisInvocation
	 * @param boundSql
	 * @param sqlParserInfo
	 * @return
	 * @throws SQLException
	 */
	private List<Map<String, Object>> query(MybatisInvocation mybatisInvocation,
										   BoundSql boundSql, SqlParserInfo sqlParserInfo) throws SQLException {
		return query(mybatisInvocation, boundSql, sqlParserInfo, -1);
	}

	private List<ChangeRowData> buildChangeDatas(String schemaName, List<Map<String, Object>> beforeResults, SqlParserInfo sqlParserInfo) {
		List<ChangeRowData> changeRowDatas = new ArrayList<>();
		if(beforeResults == null || beforeResults.isEmpty()){
			return changeRowDatas;
		}
		for (Map<String, Object> beforeDataMap : beforeResults) {
			ChangeRowData changeRowData = buildChangeDataForDelete(beforeDataMap);
			changeRowData.setSchemaName(schemaName);
			changeRowData.setTableName(sqlParserInfo.getTableName());
			changeRowData.setChangeColumnMap(changeRowData.beforeToChangeColumnMap());
			changeRowDatas.add(changeRowData);
		}
		return changeRowDatas;
	}

	private ChangeRowData buildChangeDataForDelete(final Map<String, Object> beforeDataMap) {
		List<ChangeColumnData> columnList = dataMapToColumnDataList(beforeDataMap);
		ChangeRowData changeData = new ChangeRowData();
		changeData.setBeforeColumnList(columnList);
		return changeData;
	}

}
