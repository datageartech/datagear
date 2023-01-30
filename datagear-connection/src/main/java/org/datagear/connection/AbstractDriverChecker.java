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

package org.datagear.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import org.datagear.util.JdbcUtil;

/**
 * 抽象{@linkplain DriverChecker}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDriverChecker implements DriverChecker
{
	public AbstractDriverChecker()
	{
		super();
	}

	@Override
	public boolean check(Driver driver, ConnectionOption connectionOption, boolean ignoreAcceptsURLCheck)
			throws Throwable
	{
		if (!ignoreAcceptsURLCheck)
		{
			if (!acceptsURL(driver, connectionOption.getUrl()))
				return false;
		}

		return checkConnection(driver, connectionOption);
	}

	/**
	 * 校验{@linkplain Driver}连接是否支持给定{@linkplain ConnectionOption}。
	 * <p>
	 * {@linkplain Driver#acceptsURL(String)}无需在此方法中校验。
	 * </p>
	 * 
	 * @param driver
	 * @param connectionOption
	 * @return
	 * @throws Throwable
	 */
	protected abstract boolean checkConnection(Driver driver, ConnectionOption connectionOption) throws Throwable;

	/**
	 * 获取连接。
	 * 
	 * @param driver
	 * @param connectionOption
	 * @return
	 * @throws Throwable
	 */
	protected Connection getConnection(Driver driver, ConnectionOption connectionOption) throws Throwable
	{
		return driver.connect(connectionOption.getUrl(), connectionOption.getProperties());
	}

	/**
	 * 关闭连接。
	 * 
	 * @param cn
	 */
	protected void closeConnection(Connection cn)
	{
		JdbcUtil.closeConnection(cn);
	}

	/**
	 * 判断{@linkplain Driver}是否能支持给定URL。
	 * 
	 * @param driver
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	protected boolean acceptsURL(Driver driver, String url) throws SQLException
	{
		return driver.acceptsURL(url);
	}
}
