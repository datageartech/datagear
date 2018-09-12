/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认{@linkplain ConnectionSource}实现。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultConnectionSource implements ConnectionSource
{
	private static Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSource.class);

	private DriverEntityManager driverEntityManager;

	private DriverChecker driverChecker = new SimpleDriverChecker();

	private ConcurrentMap<String, DriverEntity> urlPreferedDriverEntityMap = new ConcurrentHashMap<String, DriverEntity>();

	private volatile long driverEntityManagerLastModified = -1;

	public DefaultConnectionSource()
	{
		super();
	}

	public DefaultConnectionSource(DriverEntityManager driverEntityManager)
	{
		super();
		this.driverEntityManager = driverEntityManager;
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public DriverChecker getDriverChecker()
	{
		return driverChecker;
	}

	public void setDriverChecker(DriverChecker driverChecker)
	{
		this.driverChecker = driverChecker;
	}

	@Override
	public Connection getConnection(DriverEntity driverEntity, ConnectionOption connectionOption)
			throws ConnectionSourceException
	{
		Driver driver = this.driverEntityManager.getDriver(driverEntity);

		if (!acceptsURL(driver, connectionOption.getUrl()))
			throw new URLNotAcceptedException(driverEntity, connectionOption.getUrl());

		return getConnection(driver, connectionOption);
	}

	@Override
	public Connection getConnection(ConnectionOption connectionOption) throws ConnectionSourceException
	{
		Driver driver = findPreferredDriver(connectionOption);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Found prefered Driver [" + toDriverString(driver) + "] for [" + connectionOption + "]");

		return getConnection(driver, connectionOption);
	}

	/**
	 * 查找首选{@linkplain Driver}。
	 * 
	 * @param connectionOption
	 * @return
	 * @throws UnsupportedGetConnectionException
	 *             当找不到时抛出此异常
	 * @throws ConnectionSourceException
	 */
	protected Driver findPreferredDriver(ConnectionOption connectionOption)
			throws UnsupportedGetConnectionException, ConnectionSourceException
	{
		Driver preferedDriver = null;

		if (this.driverEntityManager.getLastModified() > this.driverEntityManagerLastModified)
		{
			this.driverEntityManagerLastModified = this.driverEntityManager.getLastModified();
			this.urlPreferedDriverEntityMap.clear();
		}

		String url = connectionOption.getUrl();

		DriverEntity preferedDriverEntity = this.urlPreferedDriverEntityMap.get(url);

		if (preferedDriverEntity != null)
		{
			try
			{
				preferedDriver = this.driverEntityManager.getDriver(preferedDriverEntity);
			}
			catch (Throwable t)
			{
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Getting Driver by cached [" + preferedDriverEntity + "]  for [" + connectionOption
							+ "] error", t);
			}
		}

		if (preferedDriver == null)
		{
			List<DriverEntityDriver> accepted = new ArrayList<DriverEntityDriver>();
			List<DriverEntityDriver> checked = new ArrayList<DriverEntityDriver>();

			List<DriverEntity> driverEntities = this.driverEntityManager.getAll();

			for (DriverEntity driverEntity : driverEntities)
			{
				Driver driver = null;

				try
				{
					driver = this.driverEntityManager.getDriver(driverEntity);
				}
				catch (Throwable t)
				{
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Getting Driver with [" + driverEntity + "] for finding prefered Driver for ["
								+ connectionOption + "] error", t);
				}

				if (driver != null)
				{
					boolean accept = false;

					try
					{
						accept = acceptsURL(driver, connectionOption.getUrl());
					}
					catch (Throwable t)
					{
					}

					if (accept)
					{
						DriverEntityDriver driverEntityDriver = new DriverEntityDriver(driverEntity, driver);
						accepted.add(driverEntityDriver);

						try
						{
							if (this.driverChecker.check(driver, connectionOption, true))
								checked.add(driverEntityDriver);
						}
						catch (Throwable t)
						{
						}
					}
				}
			}

			DriverEntityDriver preferedChecked = findPreferedDriverEntityDriver(checked);

			if (preferedChecked != null)
			{
				preferedDriver = preferedChecked.getDriver();
				this.urlPreferedDriverEntityMap.put(url, preferedChecked.getDriverEntity());
			}
			else
			{
				DriverEntityDriver preferedAccepted = findPreferedDriverEntityDriver(accepted);

				// 如果连接参数有误（比如密码错误），会导致所有校验都不通过而抛出UnsupportedGetConnectionException异常，不太合适，
				// 所以，如果没有找到校验通过的，那么就使用可接受URL的，但是不应该加入缓存
				if (preferedAccepted != null)
					preferedDriver = preferedAccepted.getDriver();
			}
		}

		if (preferedDriver == null)
			throw new UnsupportedGetConnectionException(connectionOption);

		return preferedDriver;
	}

	/**
	 * 查找首选{@linkplain DriverEntityDriver}。
	 * <p>
	 * 如果差找不到，将返回{@code null}。
	 * </p>
	 * 
	 * @param checked
	 * @return
	 */
	protected DriverEntityDriver findPreferedDriverEntityDriver(List<DriverEntityDriver> checked)
	{
		DriverEntityDriver prefered = null;

		for (DriverEntityDriver candidate : checked)
		{
			if (prefered == null)
				prefered = candidate;
			else
			{
				if (candidate.isPreferedThan(prefered))
					prefered = candidate;
			}
		}

		return prefered;
	}

	protected boolean acceptsURL(Driver driver, String url)
	{
		try
		{
			return driver.acceptsURL(url);
		}
		catch (SQLException e)
		{
			throw new ConnectionSourceException(e);
		}
	}

	/**
	 * 获取{@linkplain Connection}。
	 * 
	 * @param driver
	 * @param connectionOption
	 * @return
	 * @throws EstablishConnectionException
	 */
	protected Connection getConnection(Driver driver, ConnectionOption connectionOption)
			throws EstablishConnectionException
	{
		Properties extraProperties = getExtraConnectionProperties(driver);
		Properties properties = null;

		if (extraProperties == null)
			properties = connectionOption.getProperties();
		else
		{
			properties = new Properties(extraProperties);
			properties.putAll(connectionOption.getProperties());
		}

		try
		{
			return driver.connect(connectionOption.getUrl(), properties);
		}
		catch (SQLException e)
		{
			throw new EstablishConnectionException(connectionOption, e);
		}
	}

	protected String toDriverString(Driver driver)
	{
		return driver.getClass().getName() + "[majorVersion=" + driver.getMajorVersion() + ", minorVersion="
				+ driver.getMinorVersion() + "]";
	}

	/**
	 * 获取附加连接参数。
	 * <p>
	 * 对于{@linkplain Connection}、{@linkplain DatabaseMetaData}
	 * 中的部分接口，不同的JDBC驱动可能需要配置各自的连接参数，子类可重写此方法来实现。
	 * </p>
	 * <p>
	 * 此方法默认返回{@code null}，表示没有附加连接参数。
	 * </p>
	 * 
	 * @return
	 */
	protected Properties getExtraConnectionProperties(Driver driver)
	{
		return null;
	}

	protected static class DriverEntityDriver
	{
		private DriverEntity driverEntity;

		private Driver driver;

		public DriverEntityDriver()
		{
			super();
		}

		public DriverEntityDriver(DriverEntity driverEntity, Driver driver)
		{
			super();
			this.driverEntity = driverEntity;
			this.driver = driver;
		}

		public DriverEntity getDriverEntity()
		{
			return driverEntity;
		}

		public void setDriverEntity(DriverEntity driverEntity)
		{
			this.driverEntity = driverEntity;
		}

		public Driver getDriver()
		{
			return driver;
		}

		public void setDriver(Driver driver)
		{
			this.driver = driver;
		}

		/**
		 * 是否比给定{@linkplain DriverEntityDriver}更首选。
		 * 
		 * @param driverEntityDriver
		 * @return
		 */
		public boolean isPreferedThan(DriverEntityDriver driverEntityDriver)
		{
			int myMajorVersion = this.driver.getMajorVersion();
			int myMinorVersion = this.driver.getMinorVersion();
			int youMajorVersion = driverEntityDriver.driver.getMajorVersion();
			int youMinorVersion = driverEntityDriver.driver.getMinorVersion();

			if (myMajorVersion > youMajorVersion)
				return true;
			else if (myMajorVersion < youMajorVersion)
				return false;
			else
			{
				if (myMinorVersion > youMinorVersion)
					return true;
				else
					return false;
			}
		}
	}
}
