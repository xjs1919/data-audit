package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.mybatis.dto.ChangeRowData;
import com.github.xjs.audit.mybatis.dto.MybatisInvocation;

import java.util.List;

public interface IDataParser {
	
	/**
	 * 在执行修改之前解析数据
	 * @param commandName
	 * @param mybatisInvocation
	 * @return
	 * @throws Throwable
	 */
	List<ChangeRowData> parseBefore(String commandName, MybatisInvocation mybatisInvocation) throws Throwable;
	
	/**
	 * 在执行修改之后解析数据，如insert之后是可以取到insert的对象的id的
	 * @param mybatisInvocation
	 * @param changeRows
	 * @return
	 * @throws Throwable
	 */
	List<ChangeRowData> parseAfter(MybatisInvocation mybatisInvocation, List<ChangeRowData> changeRows)throws Throwable;
	
}
