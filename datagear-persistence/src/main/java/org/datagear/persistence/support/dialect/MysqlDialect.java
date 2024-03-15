/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.persistence.support.dialect;

import org.datagear.persistence.Order;
import org.datagear.persistence.support.AbstractDialect;
import org.datagear.util.Sql;

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
	public Sql toPagingQuerySql(Sql query, Order[] orders, long startRow, int count)
	{
		Sql sql = Sql.valueOf();

		Sql orderSql = toOrderSql(orders);

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
