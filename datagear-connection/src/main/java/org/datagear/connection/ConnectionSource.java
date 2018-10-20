/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.sql.Connection;

/**
 * JDBC连接源。
 * <p>
 * 它用于获取特定{@linkplain DriverEntity}的连接。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ConnectionSource
{
	/**
	 * 获取指定{@linkplain DriverEntity}的{@linkplain Connection}。
	 * 
	 * @param driverEntity
	 * @param connectionOption
	 * @return
	 * @throws ConnectionSourceException
	 */
	Connection getConnection(DriverEntity driverEntity, ConnectionOption connectionOption)
			throws ConnectionSourceException;

	/**
	 * 获取{@linkplain Connection}。
	 * <p>
	 * 此方法尝试获取最适合给定参数的{@linkplain Connection}。
	 * </p>
	 * 
	 * @param connectionOption
	 * @return
	 * @throws UnsupportedGetConnectionException
	 *             获取不到任何合适的{@linkplain Connection}时。
	 * @throws ConnectionSourceException
	 */
	Connection getConnection(ConnectionOption connectionOption)
			throws UnsupportedGetConnectionException, ConnectionSourceException;
}
