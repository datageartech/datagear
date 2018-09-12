/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

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
