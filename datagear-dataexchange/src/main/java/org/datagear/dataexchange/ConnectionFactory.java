/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接工厂。
 * <p>
 * 它负责在数据交换中获取和回收数据库连接，使得调用者可以灵活控制线程、连接分配。
 * </p>
 * <p>
 * {@linkplain DataImporter}、{@linkplain DataExporter}实现类在使用完这里获取的数据库连接后，不应该直接调用{@linkplain Connection#close()}方法，
 * 而要调用这里的{@linkplain #reclaimConnection(Connection)}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ConnectionFactory
{
	/**
	 * 获取数据库连接。
	 * 
	 * @return
	 * @throws SQLException
	 */
	Connection getConnection() throws SQLException;

	/**
	 * 回收数据库连接。
	 * 
	 * @param cn
	 * @throws SQLException
	 */
	void reclaimConnection(Connection cn) throws SQLException;
}
