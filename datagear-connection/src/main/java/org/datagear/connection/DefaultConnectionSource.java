/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认{@linkplain ConnectionSource}实现。
 * <p>
 * 注意：此类实例不再使用后，应该调用。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultConnectionSource implements ConnectionSource
{
	private static Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSource.class);

	private DriverEntityManager driverEntityManager;

	private DriverChecker driverChecker = new SimpleDriverChecker();

	private PropertiesProcessor propertiesProcessor = null;

	private ConcurrentMap<String, PreferedDriverEntityResult> urlPreferedDriverEntityMap = new ConcurrentHashMap<>();

	private volatile long driverEntityManagerLastModified = -1;

	private HashMap<DataSourceKey, DriverBasicDataSource> _dataSourceMap = new HashMap<>();

	private ReadWriteLock _dataSourceMapLock = new ReentrantReadWriteLock();

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

	public PropertiesProcessor getPropertiesProcessor()
	{
		return propertiesProcessor;
	}

	public void setPropertiesProcessor(PropertiesProcessor propertiesProcessor)
	{
		this.propertiesProcessor = propertiesProcessor;
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
	 * 关闭。
	 */
	public void close()
	{
		Lock writeLock = this._dataSourceMapLock.writeLock();
		try
		{
			writeLock.lock();
			Collection<DriverBasicDataSource> dataSources = this._dataSourceMap.values();
			for (DriverBasicDataSource dataSource : dataSources)
			{
				try
				{
					dataSource.close();
				}
				catch (Throwable t)
				{
					LOGGER.warn("Close data source exception:", t);
				}
			}

			this._dataSourceMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
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

		List<DriverEntityDriver> accepted = new ArrayList<>();
		List<DriverEntityDriver> checked = new ArrayList<>();

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
		Properties properties = new Properties();

		if (connectionOption.getProperties() != null)
			properties.putAll(connectionOption.getProperties());

		processConnectionProperties(driver, properties);

		try
		{
			return getConnection(driver, connectionOption.getUrl(), properties);
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

	protected Connection getConnection(Driver driver, String url, Properties properties) throws SQLException
	{
		DriverBasicDataSource dataSource = null;

		DataSourceKey dataSourceKey = new DataSourceKey(driver, url, properties);

		Lock readLock = this._dataSourceMapLock.readLock();
		try
		{
			readLock.lock();
			dataSource = this._dataSourceMap.get(dataSourceKey);
		}
		finally
		{
			readLock.unlock();
		}

		if (dataSource == null)
		{
			Lock writeLock = this._dataSourceMapLock.writeLock();
			try
			{
				writeLock.lock();
				dataSource = createDataSource(driver, url, properties);
				this._dataSourceMap.put(dataSourceKey, dataSource);
			}
			finally
			{
				writeLock.unlock();
			}
		}

		return dataSource.getConnection();
	}

	protected DriverBasicDataSource createDataSource(Driver driver, String url, Properties properties)
	{
		return new DriverBasicDataSource(driver, url, properties);
	}

	protected String toDriverString(Driver driver)
	{
		return driver.getClass().getName() + "[majorVersion=" + driver.getMajorVersion() + ", minorVersion="
				+ driver.getMinorVersion() + "]";
	}

	/**
	 * 加工连接参数。
	 * <p>
	 * 对于{@linkplain Connection}、{@linkplain DatabaseMetaData}
	 * 中的部分接口，不同的JDBC驱动可能需要配置各自的连接参数。
	 * </p>
	 * 
	 * @return
	 */
	protected void processConnectionProperties(Driver driver, Properties properties)
	{
		if (this.propertiesProcessor != null)
			this.propertiesProcessor.process(driver, properties);
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

	protected static class DataSourceKey
	{
		private Driver driver;

		private String url;

		private Properties properties;

		public DataSourceKey(Driver driver, String url, Properties properties)
		{
			super();
			this.driver = driver;
			this.url = url;
			this.properties = properties;
		}

		public Driver getDriver()
		{
			return driver;
		}

		protected void setDriver(Driver driver)
		{
			this.driver = driver;
		}

		public String getUrl()
		{
			return url;
		}

		protected void setUrl(String url)
		{
			this.url = url;
		}

		public Properties getProperties()
		{
			return properties;
		}

		protected void setProperties(Properties properties)
		{
			this.properties = properties;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((driver == null) ? 0 : driver.hashCode());
			result = prime * result + ((properties == null) ? 0 : properties.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataSourceKey other = (DataSourceKey) obj;
			if (driver == null)
			{
				if (other.driver != null)
					return false;
			}
			else if (!driver.equals(other.driver))
				return false;
			if (properties == null)
			{
				if (other.properties != null)
					return false;
			}
			else if (!properties.equals(other.properties))
				return false;
			if (url == null)
			{
				if (other.url != null)
					return false;
			}
			else if (!url.equals(other.url))
				return false;
			return true;
		}
	}

	/**
	 * 基于{@linkplain Driver}实例的{@linkplain BasicDataSource}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class DriverBasicDataSource extends BasicDataSource
	{
		private Driver driver;

		public DriverBasicDataSource(Driver driver, String url, Properties properties)
		{
			super();
			this.driver = driver;
			super.setDriverClassName(driver.getClass().getName());
			super.setUrl(url);
			super.connectionProperties = properties;
		}

		protected Driver getDriver()
		{
			return driver;
		}

		protected void setDriver(Driver driver)
		{
			this.driver = driver;
		}

		@Override
		protected ConnectionFactory createConnectionFactory() throws SQLException
		{
			return new DriverConnectionFactory(driver, url, connectionProperties);
		}
	}
}
