/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.dialect;

/**
 * 默认{@linkplain MbSqlDialect}。
 * <p>
 * 此方言不支持分页查询（{@linkplain #supportsPaging()}返回{@code false}）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultMbSqlDialect extends MbSqlDialect
{
	public DefaultMbSqlDialect()
	{
		super();
	}

	public DefaultMbSqlDialect(String identifierQuote)
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
}
