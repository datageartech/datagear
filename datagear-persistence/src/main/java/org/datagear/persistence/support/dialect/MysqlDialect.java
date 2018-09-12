/*
 * Copyright (c) 2018 by datagear.org.
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
	public SqlBuilder toPagingSql(SqlBuilder queryView, SqlBuilder condition, Order[] orders, long startRow, int count)
	{
		SqlBuilder sql = SqlBuilder.valueOf();

		SqlBuilder orderSql = toOrderSql(orders);

		if (isEmptySql(condition) && isEmptySql(orderSql))
		{
			sql.sql(queryView);
		}
		else
		{
			sql.sql("SELECT * FROM (");
			sql.sql(queryView);
			sql.sql(") t ");

			if (!isEmptySql(condition))
			{
				sql.sql(" WHERE ");
				sql.sql(condition);
			}

			if (!isEmptySql(orderSql))
			{
				sql.sql(" ORDER BY ");
				sql.sql(orderSql);
			}
		}

		sql.sql(" LIMIT " + (startRow - 1) + ", " + count);

		return sql;
	}
}
