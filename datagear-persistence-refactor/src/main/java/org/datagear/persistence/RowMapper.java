/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
	 * @param row   行号，以{@code 1}开始
	 * @return
	 * @throws RowMapperException
	 */
	Row map(Connection cn, Table table, ResultSet rs, int row) throws RowMapperException;
}
