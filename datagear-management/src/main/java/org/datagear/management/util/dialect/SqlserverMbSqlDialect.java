/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	public String funcNameReplace()
	{
		return DEFAULT_FUNC_PREFIX + super.funcNameReplace();
	}

	@Override
	public String funcNameModInt()
	{
		return DEFAULT_FUNC_PREFIX + super.funcNameModInt();
	}

	@Override
	public String funcNameLength()
	{
		return DEFAULT_FUNC_PREFIX + super.funcNameLength();
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
}
