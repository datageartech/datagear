/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapper;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.SqlParamValue;

/**
 * 简单{@linkplain SqlParamValueMapper}。
 * <p>
 * 它什么也不做，直接返回原值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleSqlParamValueMapper extends AbstractSqlParamValueMapper
{
	public SimpleSqlParamValueMapper()
	{
		super();
	}

	@Override
	public SqlParamValue map(Connection cn, Table table, Column column, Object value)
			throws SqlParamValueMapperException
	{
		return createSqlParamValue(column, value);
	}
}
