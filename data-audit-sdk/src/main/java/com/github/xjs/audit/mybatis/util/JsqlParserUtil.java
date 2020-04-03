package com.github.xjs.audit.mybatis.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;

import java.util.ArrayList;
import java.util.List;

public class JsqlParserUtil {
	
	public static List<String> getWhereColumn(Expression whereExpression) {
		final List<String> result = new ArrayList<>();
		whereExpression.accept(new ExpressionVisitorAdapter() {
			@Override
			public void visit(Column expr) {
				if (!result.contains(expr.getColumnName())) {
					if (expr.getColumnName().indexOf(".") == -1 && expr.getColumnName().toLowerCase().indexOf("id") > -1) {
						result.add("id");
					}else{
						result.add(MybatisUtils.mapUnderscoreToCamelCase(expr.getColumnName().toLowerCase()));
					}
				}
			}

		});
		return result;
	}
	
	public static Select getSelect(Table table, List<Column> column, Expression whereExpression){
		Column[] selectColumns = (Column[]) column.toArray(new Column[column.size()]);
		Select select = SelectUtils.buildSelectFromTableAndExpressions(table, selectColumns);
		PlainSelect selectPlain = (PlainSelect) select.getSelectBody(); 
		selectPlain.setWhere(whereExpression);
		return select;
	}
	
}

