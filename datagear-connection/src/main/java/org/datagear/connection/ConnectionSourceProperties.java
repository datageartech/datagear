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

import java.io.Serializable;

import org.apache.commons.dbcp2.BasicDataSource;
import org.datagear.connection.DefaultConnectionSource.InternalDataSourceKey;

/**
 * {@linkplain DefaultConnectionSource}配置信息。
 * 
 * @author datagear@163.com
 *
 */
public class ConnectionSourceProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_CACHE_MAX_SIZE = 50;

	/**
	 * 默认缓存过期秒数。
	 * <p>
	 * 这里不应设置过长，因为{@linkplain #internalDataSourceCache}的关键字{@linkplain InternalDataSourceKey#getDriver()}，
	 * 可能会因为对{@linkplain DriverEntityManager}的编辑而被新的类加载器加载，导致新的{@linkplain InternalDataSourceKey}与旧的不同，
	 * 此时，旧的应尽早从缓存中删除释放。
	 * </p>
	 */
	public static final int DEFAULT_CACHE_EXPIRED_SECONDS = 60 * 10;

	private int cacheMaxSize = DEFAULT_CACHE_MAX_SIZE;

	private int cacheExpiredSeconds = DEFAULT_CACHE_EXPIRED_SECONDS;

	/**
	 * 底层数据源配置项：{@linkplain BasicDataSource#setMaxTotal(int)}
	 */
	private Integer internalMaxTotal = null;

	/**
	 * 底层数据源配置项：{@linkplain BasicDataSource#setMaxIdle(int)}
	 */
	private Integer internalMaxIdle = null;

	/**
	 * 底层数据源配置项：{@linkplain BasicDataSource#setMinIdle(int)}
	 */
	private Integer internalMinIdle = null;

	/**
	 * 底层数据源配置项：{@linkplain BasicDataSource#setInitialSize(int)}
	 */
	private Integer internalInitialSize = null;

	/**
	 * 底层数据源配置项：{@linkplain BasicDataSource#setMaxWaitMillis(long)}
	 */
	private Long internalMaxWaitMillis = null;

	public ConnectionSourceProperties()
	{
		super();
	}

	public int getCacheMaxSize()
	{
		return cacheMaxSize;
	}

	public void setCacheMaxSize(int cacheMaxSize)
	{
		this.cacheMaxSize = cacheMaxSize;
	}

	public int getCacheExpiredSeconds()
	{
		return cacheExpiredSeconds;
	}

	public void setCacheExpiredSeconds(int cacheExpiredSeconds)
	{
		this.cacheExpiredSeconds = cacheExpiredSeconds;
	}

	public Integer getInternalMaxTotal()
	{
		return internalMaxTotal;
	}

	public void setInternalMaxTotal(Integer internalMaxTotal)
	{
		this.internalMaxTotal = internalMaxTotal;
	}

	public Integer getInternalMaxIdle()
	{
		return internalMaxIdle;
	}

	public void setInternalMaxIdle(Integer internalMaxIdle)
	{
		this.internalMaxIdle = internalMaxIdle;
	}

	public Integer getInternalMinIdle()
	{
		return internalMinIdle;
	}

	public void setInternalMinIdle(Integer internalMinIdle)
	{
		this.internalMinIdle = internalMinIdle;
	}

	public Integer getInternalInitialSize()
	{
		return internalInitialSize;
	}

	public void setInternalInitialSize(Integer internalInitialSize)
	{
		this.internalInitialSize = internalInitialSize;
	}

	public Long getInternalMaxWaitMillis()
	{
		return internalMaxWaitMillis;
	}

	public void setInternalMaxWaitMillis(Long internalMaxWaitMillis)
	{
		this.internalMaxWaitMillis = internalMaxWaitMillis;
	}
}
