/*
 * Copyright 2018-present datagear.tech
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
