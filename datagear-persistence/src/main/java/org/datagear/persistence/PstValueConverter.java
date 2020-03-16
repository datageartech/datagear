/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.datagear.meta.Column;
import org.datagear.meta.Table;

/**
 * 对象至{@linkplain PreparedStatement}可用值转换器。
 * 
 * @author datagear@163.com
 *
 */
public interface PstValueConverter
{
	/**
	 * 转换。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws PstValueConverterException
	 */
	Object convert(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstValueConverterException;
}
