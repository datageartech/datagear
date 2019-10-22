/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support.dialect;

import org.datagear.persistence.Order;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.support.AbstractDialect;

/**
 * Mysql方言。
 * 
 * @author datagear@163.com
 *
 */
public class MysqlDialect extends AbstractDialect
{
	public MysqlDialect()
	{
		super();
	}

	public MysqlDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPagingSql()
	{
		return true;
	}

	@Override
	public SqlBuilder toPagingQuerySql(SqlBuilder query, Order[] orders, long startRow, int count)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		SqlBuilder orderSql = toOrderSql(orders);

		if (isEmptySql(orderSql))
		{
			sql.sql(query);
		}
		else
		{
			sql.sql("SELECT * FROM (");
			sql.sql(query);
			sql.sql(") T ");
			sql.sql(" ORDER BY ");
			sql.sql(orderSql);
		}

		sql.sql(" LIMIT " + (startRow - 1) + ", " + count);

		return sql;
	}
}
