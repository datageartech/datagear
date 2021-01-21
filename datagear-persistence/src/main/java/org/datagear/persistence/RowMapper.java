/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
