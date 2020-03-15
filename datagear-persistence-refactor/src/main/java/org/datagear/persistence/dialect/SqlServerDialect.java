/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.dialect;

import org.datagear.persistence.AbstractDialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.Sql;

/**
 * SqlServer方言。
 * 
 * @author datagear@163.com
 *
 */
public class SqlServerDialect extends AbstractDialect
{
	public SqlServerDialect()
	{
		super();
	}

	public SqlServerDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPagingSql()
	{
		return true;
	}

	@Override
	public Sql toPagingQuerySql(Sql query, Order[] orders, long startRow, int count)
	{
		// SqlServer分页需要排序字段
		if (orders == null || orders.length == 0)
			return null;

		Sql sql = Sql.valueOf();

		Sql orderSql = toOrderSql(orders);

		sql.sql("SELECT T1.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY ").sql(orderSql).sql(") AS ROWNUM_____, T0.* ");
		sql.sql(" FROM (").sql(query).sql(
				") T0 ) T1 WHERE (T1.ROWNUM_____ >= " + startRow + " AND T1.ROWNUM_____ < " + (startRow + count) + ")");

		return sql;
	}
}
