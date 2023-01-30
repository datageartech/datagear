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
