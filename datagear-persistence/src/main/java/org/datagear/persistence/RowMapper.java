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
import java.sql.ResultSet;

import org.datagear.meta.Table;

/**
 * {@linkplain ResultSet}至{@linkplain Row}行映射器。
 * 
 * @author datagear@163.com
 *
 */
public interface RowMapper
{
	/**
	 * 将结果集行映射为{@linkplain Row}。
	 * 
	 * @param cn
	 * @param table
	 * @param rs
	 * @param rowIndex   行号，以{@code 1}开始
	 * @return
	 * @throws RowMapperException
	 */
	Row map(Connection cn, Table table, ResultSet rs, int rowIndex) throws RowMapperException;
}
