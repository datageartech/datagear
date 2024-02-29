/*
 * Copyright 2018-2024 datagear.tech
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
