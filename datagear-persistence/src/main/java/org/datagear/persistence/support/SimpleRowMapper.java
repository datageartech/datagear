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
