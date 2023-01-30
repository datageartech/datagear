/*
 * Copyright 2018-2023 datagear.tech
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
 * SQL Server 方言。
 * 
 * @author datagear@163.com
 *
 */
public class SqlserverMbSqlDialect extends MbSqlDialect
{
	public static final String DEFAULT_FUNC_PREFIX = "dbo.";

	public SqlserverMbSqlDialect()
	{
		super();
	}

	public SqlserverMbSqlDialect(String identifierQuote)
	{
		super(identifierQuote);
	}

	@Override
	public boolean supportsPaging()
	{
		return false;
	}

	@Override
	public String pagingSqlHead(int index, int fetchSize)
	{
		return null;
	}

	@Override
	public String pagingSqlFoot(int index, int fetchSize)
	{
		return null;
	}

	@Override
	public String funcNameModInt()
	{
		return DEFAULT_FUNC_PREFIX + super.funcNameModInt();
	}
}
