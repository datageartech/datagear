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

package org.datagear.management.util.dialect;

/**
 * Oracle 方言。
 * 
 * @author datagear@163.com
 *
 */
public class OracleMbSqlDialect extends MbSqlDialect
{
	public OracleMbSqlDialect()
	{
		super();
	}

	public OracleMbSqlDialect(String identifierQuote)
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
		return "SELECT PGQ2.* FROM (SELECT PGQ1.*, ROWNUM AS ROWNUM____ FROM (";
	}

	@Override
	public String pagingSqlFoot(int index, int fetchSize)
	{
		return ") PGQ1 WHERE ROWNUM <= " + (index + fetchSize) + ") PGQ2 WHERE ROWNUM____ > " + index;
	}
}
