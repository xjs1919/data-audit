package com.github.xjs.audit.mybatis.dto;

import com.github.xjs.audit.mybatis.DBActionTypeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

public class SqlParserInfo {
	private DBActionTypeEnum actionType;
	private String tableName;
	private String schemaName;
	private Table table;
	private Expression whereExpression;

	//只有update才需要
	private List<Column> columns;
	private List<Expression> expressions;
	
	public SqlParserInfo(String sql, DBActionTypeEnum actionType) throws JSQLParserException {
		this.actionType = actionType;
		if(sql == null || sql.length()<=0){
			return ;
		}
		Statement statement = CCJSqlParserUtil.parse(sql);
		if(actionType == DBActionTypeEnum.UPDATE){
			Update updateStatement = (Update) statement;
			this.table = updateStatement.getTable();
			this.whereExpression = updateStatement.getWhere();

			this.columns = updateStatement.getColumns();
			this.expressions = updateStatement.getExpressions();
		}else if(actionType == DBActionTypeEnum.INSERT){
			Insert insertStatement = (Insert) statement;
			Table insertTable = insertStatement.getTable();
			if(insertTable==null ){
				return ;
			}
			this.table = insertTable;
			this.whereExpression = null;
		}else if(actionType == DBActionTypeEnum.DELETE){
			Delete deleteStatement = (Delete) statement;
			Table deleteTables = deleteStatement.getTable();
			if(deleteTables==null ){
				return ;
			}
			this.table = deleteTables;
			this.whereExpression = deleteStatement.getWhere();
		}
		//防止表名中带有schema
		this.tableName = table.getName();
		this.schemaName = table.getSchemaName();
	}


	public DBActionTypeEnum getActionType() {
		return actionType;
	}

	public void setActionType(DBActionTypeEnum actionType) {
		this.actionType = actionType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public Expression getWhereExpression() {
		return whereExpression;
	}

	public void setWhereExpression(Expression whereExpression) {
		this.whereExpression = whereExpression;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public List<Expression> getExpressions() {
		return expressions;
	}

	public void setExpressions(List<Expression> expressions) {
		this.expressions = expressions;
	}
}

