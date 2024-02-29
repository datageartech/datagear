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

package org.datagear.management.util.dialect;

/**
 * MySQL 方言。
 * 
 * @author datagear@163.com
 *
 */
public class MysqlMbSqlDialect extends MbSqlDialect
{
	public MysqlMbSqlDialect()
	{
		super();
	}

	public MysqlMbSqlDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPaging()
	{
		return true;
	}

	@Override
	public String pagingSqlHead(int index, int fetchSize)
	{
		return "SELECT PGQ.* FROM (";
	}

	@Override
	public String pagingSqlFoot(int index, int fetchSize)
	{
		return ") PGQ LIMIT " + index + "," + fetchSize;
	}
}
