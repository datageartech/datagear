/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	private ConcurrentMap<String, PreferedDriverEntityResult> urlPreferedDriverEntityMap = new ConcurrentHashMap<String, PreferedDriverEntityResult>();

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
		return getPreferredConnection(connectionOption);
	}

	/**
	 * 获取首选{@linkplain Connection}。
	 * 
	 * @param connectionOption
	 * @return
	 * @throws UnsupportedGetConnectionException
	 *             当找不到时抛出此异常
	 * @throws ConnectionSourceException
	 */
	protected Connection getPreferredConnection(ConnectionOption connectionOption)
			throws UnsupportedGetConnectionException, ConnectionSourceException
	{
		if (this.driverEntityManager.getLastModified() > this.driverEntityManagerLastModified)
		{
			this.driverEntityManagerLastModified = this.driverEntityManager.getLastModified();
			this.urlPreferedDriverEntityMap.clear();
		}

		String url = connectionOption.getUrl();

		PreferedDriverEntityResult preferedDriverEntityResult = this.urlPreferedDriverEntityMap.get(url);

		if (preferedDriverEntityResult != null)
		{
			if (preferedDriverEntityResult.hasDriverEntity())
			{
				DriverEntity preferedDriverEntity = preferedDriverEntityResult.getDriverEntity();

				Driver preferedDriver = this.driverEntityManager.getDriver(preferedDriverEntity);
				Connection preferedConnection = getConnection(preferedDriver, connectionOption);

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Got prefered connection by cached [" + preferedDriverEntity + "] for ["
							+ connectionOption + "]");

				return preferedConnection;
			}
			else
			{
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Got null connection by cached no-prefered " + DriverEntity.class.getSimpleName()
							+ " for [" + connectionOption + "]");

				throw new UnsupportedGetConnectionException(connectionOption);
			}
		}

		Connection preferedConnection = null;

		List<DriverEntityDriver> accepted = new ArrayList<DriverEntityDriver>();
		List<DriverEntityDriver> checked = new ArrayList<DriverEntityDriver>();

		findOrderedAcceptedAndCheckedDriverEntityDrivers(connectionOption, accepted, checked);

		// 如果仅使用checked列表，那么当连接参数有误时（比如密码错误），所有checked都无法通过从而导致抛出UnsupportedGetConnectionException，
		// 这样就无法向用户展示连接错误信息，所以下面加入一个accepted元素，用于在这种情况是抛出友好的异常
		if (accepted.size() > 0)
			checked.add(accepted.get(0));

		for (int i = 0, len = checked.size(); i < len; i++)
		{
			DriverEntityDriver driverEntityDriver = checked.get(i);
			DriverEntity driverEntity = driverEntityDriver.getDriverEntity();

			try
			{
				preferedConnection = getConnection(driverEntityDriver.getDriver(), connectionOption);

				this.urlPreferedDriverEntityMap.put(connectionOption.getUrl(),
						new PreferedDriverEntityResult(driverEntity));

				break;
			}
			catch (ConnectionSourceException e)
			{
				if (i == len - 1)
				{
					// 使用最后一个最为首选，这样下次获取时，可以使用缓存中的它，直接抛出异常供上层应用知晓，不用再查找一次
					this.urlPreferedDriverEntityMap.put(connectionOption.getUrl(),
							new PreferedDriverEntityResult(driverEntity));

					// 抛出最后一个异常，供上层应用知晓
					throw e;
				}
				else
				{
					if (LOGGER.isDebugEnabled())
						LOGGER.debug(
								"Getting connection with [" + driverEntity + "]  for [" + connectionOption + "] error",
								e);
				}
			}
		}

		if (preferedConnection == null)
		{
			this.urlPreferedDriverEntityMap.put(connectionOption.getUrl(), new PreferedDriverEntityResult());

			throw new UnsupportedGetConnectionException(connectionOption);
		}
		else
			return preferedConnection;
	}

	/**
	 * 查找经过首选优先级排序接受和校验的{@linkplain DriverEntityDriver}列表。
	 * <p>
	 * 越靠前的首选优先级越高。
	 * </p>
	 * 
	 * @param connectionOption
	 * @param accepted
	 * @param checked
	 */
	protected void findOrderedAcceptedAndCheckedDriverEntityDrivers(ConnectionOption connectionOption,
			List<DriverEntityDriver> accepted, List<DriverEntityDriver> checked)
	{
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
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Getting Driver with [" + driverEntity + "] for getting prefered connection for ["
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
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Checking if url accepted with [" + driverEntity
								+ "] for getting prefered connection for [" + connectionOption + "] error", t);
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
						if (LOGGER.isDebugEnabled())
							LOGGER.debug("checking if [" + driverEntity
									+ "] 's driver checked for getting prefered connection for [" + connectionOption
									+ "] error", t);
					}
				}
			}
		}

		Comparator<DriverEntityDriver> comparator = new Comparator<DriverEntityDriver>()
		{
			@Override
			public int compare(DriverEntityDriver o1, DriverEntityDriver o2)
			{
				Driver d1 = o1.getDriver();
				Driver d2 = o2.getDriver();

				if (isHigherVersion(d1, d2))
					return -1;
				else if (isHigherVersion(d2, d1))
					return 1;
				else
					return 0;
			}
		};

		Collections.sort(accepted, comparator);
		Collections.sort(checked, comparator);
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
	 * @throws ConnectionSourceException
	 */
	protected Connection getConnection(Driver driver, ConnectionOption connectionOption)
			throws EstablishConnectionException, ConnectionSourceException
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
		catch (Throwable t)
		{
			throw new ConnectionSourceException(t);
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

	/**
	 * {@code driver1}的版本是否高于{@linkplain driver2}。
	 * 
	 * @param driver1
	 * @param driver2
	 * @return
	 */
	protected boolean isHigherVersion(Driver driver1, Driver driver2)
	{
		int myMajorVersion = driver1.getMajorVersion();
		int myMinorVersion = driver1.getMinorVersion();
		int youMajorVersion = driver2.getMajorVersion();
		int youMinorVersion = driver2.getMinorVersion();

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

	protected static class PreferedDriverEntityResult
	{
		private DriverEntity driverEntity;

		public PreferedDriverEntityResult()
		{
			super();
		}

		public PreferedDriverEntityResult(DriverEntity driverEntity)
		{
			super();
			this.driverEntity = driverEntity;
		}

		public boolean hasDriverEntity()
		{
			return (this.driverEntity != null);
		}

		public DriverEntity getDriverEntity()
		{
			return driverEntity;
		}

		public void setDriverEntity(DriverEntity driverEntity)
		{
			this.driverEntity = driverEntity;
		}
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
	}
}
