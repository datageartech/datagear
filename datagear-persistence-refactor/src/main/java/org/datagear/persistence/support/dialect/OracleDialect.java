/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support.dialect;

import org.datagear.persistence.Order;
import org.datagear.persistence.Sql;
import org.datagear.persistence.support.AbstractDialect;

/**
 * Oracle方言。
 * 
 * @author datagear@163.com
 *
 */
public class OracleDialect extends AbstractDialect
{
	public OracleDialect()
	{
		super();
	}

	public OracleDialect(String identifierQuote)
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
		Sql sql = Sql.valueOf();

		Sql orderSql = toOrderSql(orders);

		sql.sql("SELECT T2.* FROM (SELECT T1.*, ROWNUM AS ROWNUM_____ FROM (");

		if (isEmptySql(orderSql))
		{
			sql.sql(query);
		}
		else
		{
			sql.sql("SELECT * FROM (");
			sql.sql(query);
			sql.sql(") T0 ");

			if (!isEmptySql(orderSql))
			{
				sql.sql(" ORDER BY ");
				sql.sql(orderSql);
			}
		}

		sql.sql(") T1 WHERE ROWNUM < " + (startRow + count));
		sql.sql(") T2 WHERE ROWNUM_____ >= " + startRow);

		return sql;
	}
}
