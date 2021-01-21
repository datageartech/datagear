/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.RowMapper;

/**
 * 简单{@linkplain RowMapper}。
 * <p>
 * 它直接返回JDBC规范的默认值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleRowMapper extends AbstractRowMapper
{
	public SimpleRowMapper()
	{
		super();
	}

	@Override
	protected Object mapColumn(Connection cn, Table table, ResultSet rs, int rowIndex, Column column) throws Throwable
	{
		return getColumnValue(cn, rs, column);
	}
}
