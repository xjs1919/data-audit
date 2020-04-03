package com.github.xjs.audit.mybatis.dto;

public class ChangeColumnData {
	
	public String name;
	public Object value;

	public ChangeColumnData(){}

	public ChangeColumnData(String name, Object value){
		this.name = name;
		this.value =value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ChangeColumnData{" +
				"name='" + name + '\'' +
				", value=" + value +
				'}';
	}
}
