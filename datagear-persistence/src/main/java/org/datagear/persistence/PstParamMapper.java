/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * 对象至{@linkplain PreparedStatement}参数值映射器。
 * 
 * @author datagear@163.com
 *
 */
public interface PstParamMapper
{
	/**
	 * 映射。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws PstParamMapperException
	 */
	Object map(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstParamMapperException;
}
