package com.github.xjs.audit.mybatis.parser;

import com.github.xjs.audit.mybatis.DBActionTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class ParserFactory {

	private ParserFactory() {
	}

	private static Map<String, IDataParser> parserMap = new HashMap<>();

	static {
		parserMap.put(DBActionTypeEnum.UPDATE.getValue(), new UpdateParser());
		parserMap.put(DBActionTypeEnum.INSERT.getValue(), new InsertParser());
		parserMap.put(DBActionTypeEnum.DELETE.getValue(), new DeleteParser());
	}

	public static IDataParser getDataParser(String commandName) {
		return parserMap.get(commandName);
	}
}
