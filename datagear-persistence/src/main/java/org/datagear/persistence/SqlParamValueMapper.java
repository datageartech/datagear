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

package org.datagear.persistence;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.util.SqlParamValue;

/**
 * 列值至{@linkplain SqlParamValue}映射器。
 * 
 * @author datagear@163.com
 *
 */
public interface SqlParamValueMapper
{
	/**
	 * 映射。
	 * <p>
	 * 此方法的返回值可能是{@linkplain LiteralSqlParamValue}。
	 * </p>
	 * <p>
	 * 注意：此方法不应返回{@code null}，返回的{@linkplain SqlParamValue#getValue()}允许为{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 *            允许为{@code null}
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	SqlParamValue map(Connection cn, Table table, Column column, Object value) throws SqlParamValueMapperException;
}
