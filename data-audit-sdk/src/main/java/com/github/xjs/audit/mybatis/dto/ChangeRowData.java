package com.github.xjs.audit.mybatis.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表的变化数据
 * */
public class ChangeRowData {

	/** 数据库库名 */
	public String schemaName;

	/** 数据库表名 */
	public String tableName;

	/** 对应的数据库表的主键ID*/
	public Object entityId;

	/** 更改的所有属性的以及它对应的原始值,即更改之前的值*/
	public List<ChangeColumnData> beforeColumnList;

	/**更改的所有属性和值*/
	public List<ChangeColumnData> afterColumnList;

	/** 发生变化的对象的属性及值的键值对*/
	public Map<String, ChangeData> changeColumnMap;

	public Map<String, ChangeData> getChangeColumnMap() {
		return changeColumnMap;
	}

	public void setChangeColumnMap(Map<String, ChangeData> changeColumnMap) {
		this.changeColumnMap = changeColumnMap;
	}

	public List<ChangeColumnData> getAfterColumnList() {
		return afterColumnList;
	}

	public void setAfterColumnList(List<ChangeColumnData> afterColumnList) {
		this.afterColumnList = afterColumnList;
	}

	public List<ChangeColumnData> getBeforeColumnList() {
		return beforeColumnList;
	}

	public void setBeforeColumnList(List<ChangeColumnData> beforeColumnList) {
		this.beforeColumnList = beforeColumnList;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Object getEntityId() {
		return entityId;
	}

	public void setEntityId(Object entityId) {
		this.entityId = entityId;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public Map<String, ChangeData> beforeToChangeColumnMap(){
		List<ChangeColumnData> beforeList = this.beforeColumnList;
		if(beforeList == null || beforeList.size() <= 0){
			return null;
		}
		Map<String, ChangeData> map = new HashMap<>();
		for(ChangeColumnData before : beforeList){
			String name = before.getName();
			Object value = before.getValue();
			map.put(name, new ChangeData(name, value, null));
		}
		return map;
	}

	public Map<String, ChangeData> afterToChangeColumnMap(){
		List<ChangeColumnData> afterList = this.afterColumnList;
		if(afterList == null || afterList.size() <= 0){
			return null;
		}
		Map<String, ChangeData> map = new HashMap<>();
		for(ChangeColumnData after : afterList){
			String name = after.getName();
			Object value = after.getValue();
			map.put(name, new ChangeData(name, null, value));
		}
		return map;
	}

	@Override
	public String toString() {
		return "ChangeRowData{" +
				"schemaName='" + schemaName + '\'' +
				", tableName='" + tableName + '\'' +
				", entityId=" + entityId +
				", beforeColumnList=" + beforeColumnList +
				", afterColumnList=" + afterColumnList +
				", changeColumnMap=" + changeColumnMap +
				'}';
	}
}
