/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
