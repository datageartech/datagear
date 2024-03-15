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

import java.io.IOException;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

/**
 * 默认{@linkplain ConnectionSource}实现。
 * <p>
 * 注意：此类实例不再使用后，应该调用{@linkplain #close()}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultConnectionSource implements ConnectionSource, AutoCloseable
{
	private static Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSource.class);

	/**
	 * 默认内置数据源过期秒数。
	 * <p>
	 * 这里不应设置过长，因为{@linkplain #internalDataSourceCache}的关键字{@linkplain InternalDataSourceKey#getDriver()}，
	 * 可能会因为对{@linkplain DriverEntityManager}的编辑而被新的类加载器加载，导致新的{@linkplain InternalDataSourceKey}与旧的不同，
	 * 此时，旧的应尽早从缓存中删除释放。
	 * </p>
	 */
	public static final int DEFAULT_INTERNAL_DS_EXPIRED_SECONDS = 60 * 10;

	public static final int DEFAULT_CACHE_SIZE = 50;

	private DriverEntityManager driverEntityManager;

	private DriverChecker driverChecker = new SimpleDriverChecker();

	private PropertiesProcessor propertiesProcessor = null;

	private Cache<InternalDataSourceKey, InternalDataSourceHolder> internalDataSourceCache;

	private ConcurrentMap<String, PreferedDriverEntity> _urlPreferedDriverEntities = new ConcurrentHashMap<>();
	private volatile long _driverEntityManagerLastModified = -1;

	public DefaultConnectionSource()
	{
		this(null);
	}

	public DefaultConnectionSource(DriverEntityManager driverEntityManager)
	{
		this(driverEntityManager, null, null);
	}

	public DefaultConnectionSource(DriverEntityManager driverEntityManager, Integer internalDsExpiredSeconds,
			Integer maxCacheSize)
	{
		super();
		this.driverEntityManager = driverEntityManager;

		if (internalDsExpiredSeconds == null)
			internalDsExpiredSeconds = DEFAULT_INTERNAL_DS_EXPIRED_SECONDS;

		if (maxCacheSize == null)
			maxCacheSize = DEFAULT_CACHE_SIZE;

		this.internalDataSourceCache = Caffeine.newBuilder().maximumSize(maxCacheSize)
				.expireAfterAccess(internalDsExpiredSeconds, TimeUnit.SECONDS)
				.scheduler(Scheduler.forScheduledExecutorService(Executors.newScheduledThreadPool(1)))
				.removalListener(new DriverBasicDataSourceRemovalListener()).build();
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

	protected Cache<InternalDataSourceKey, InternalDataSourceHolder> getInternalDataSourceCache()
	{
		return this.internalDataSourceCache;
	}

	/**
	 * 设置内置数据源缓存。
	 * <p>
	 * 注意：设置的缓存应自己注册{@linkplain RemovalListener}负责关闭内置数据源。
	 * </p>
	 * 
	 * @param driverBasicDataSourceCache
	 */
	protected void setInternalDataSourceCache(
			Cache<InternalDataSourceKey, InternalDataSourceHolder> internalDataSourceCache)
	{
		this.internalDataSourceCache = internalDataSourceCache;
	}

	protected ConcurrentMap<String, PreferedDriverEntity> getUrlPreferedDriverEntities()
	{
		return _urlPreferedDriverEntities;
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
	@Override
	public void close()
	{
		this.internalDataSourceCache.invalidateAll();
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
		if (this.driverEntityManager.getLastModified() != this._driverEntityManagerLastModified)
		{
			this._driverEntityManagerLastModified = this.driverEntityManager.getLastModified();
			this._urlPreferedDriverEntities.clear();
		}

		String url = connectionOption.getUrl();

		PreferedDriverEntity preferedDriverEntity = this._urlPreferedDriverEntities.get(url);

		if (preferedDriverEntity != null)
		{
			if (preferedDriverEntity.hasDriverEntityDriver())
			{
				DriverEntityDriver driverEntityDriver = preferedDriverEntity.getDriverEntityDriver();
				DriverEntity driverEntity = driverEntityDriver.getDriverEntity();
				long driverEntityLastModified = this.driverEntityManager.getLastModified(driverEntity);

				if (driverEntityLastModified > -1
						&& driverEntityLastModified == preferedDriverEntity.getCreationModified())
				{
					Driver driver = driverEntityDriver.getDriver();
					Connection preferedConnection = getConnection(driver, connectionOption);

					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Get prefered connection by cached [" + preferedDriverEntity + "] for ["
								+ connectionOption.copyOfPsdMask() + "]");

					return preferedConnection;
				}
			}
			else
			{
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Get null connection by cached no-prefered " + DriverEntity.class.getSimpleName()
							+ " for [" + connectionOption.copyOfPsdMask() + "]");

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

			try
			{
				preferedConnection = getConnection(driverEntityDriver.getDriver(), connectionOption);

				this._urlPreferedDriverEntities.put(connectionOption.getUrl(),
						createPreferedDriverEntity(driverEntityDriver));

				break;
			}
			catch (ConnectionSourceException e)
			{
				if (i == len - 1)
				{
					// 使用最后一个最为首选，这样下次获取时，可以使用缓存中的它，直接抛出异常供上层应用知晓，不用再查找一次
					this._urlPreferedDriverEntities.put(url, createPreferedDriverEntity(driverEntityDriver));

					// 抛出最后一个异常，供上层应用知晓
					throw e;
				}
				else
				{
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Get connection with [" + driverEntityDriver.getDriverEntity() + "]  for ["
								+ connectionOption.copyOfPsdMask() + "] error", e);
				}
			}
		}

		if (preferedConnection == null)
		{
			this._urlPreferedDriverEntities.put(url, createPreferedDriverEntity(null));
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
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Get Driver with [" + driverEntity + "] for getting prefered connection for ["
							+ connectionOption.copyOfPsdMask() + "] error", t);
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
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Check if url accepted with [" + driverEntity
								+ "] for getting prefered connection for [" + connectionOption.copyOfPsdMask()
								+ "] error", t);
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
						if (LOGGER.isErrorEnabled())
							LOGGER.error("Check if [" + driverEntity
									+ "] 's driver checked for getting prefered connection for ["
									+ connectionOption.copyOfPsdMask()
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
		String url = connectionOption.getUrl();
		Properties properties = new Properties();

		if (connectionOption.getProperties() != null)
			properties.putAll(connectionOption.getProperties());

		processConnectionProperties(driver, url, properties);

		try
		{
			return getConnection(driver, url, properties);
		}
		catch (SQLException | ExecutionException e)
		{
			throw new EstablishConnectionException(connectionOption, e);
		}
		catch (Throwable t)
		{
			throw new ConnectionSourceException(t);
		}
	}

	protected Connection getConnection(Driver driver, String url, Properties properties)
			throws ExecutionException, SQLException, Throwable
	{
		InternalDataSourceKey key = new InternalDataSourceKey(driver, url, properties);

		Connection connection = null;
		InternalDataSourceHolder dataSourceHolder = null;

		try
		{
			dataSourceHolder = this.internalDataSourceCache.get(key,
					new Function<InternalDataSourceKey, InternalDataSourceHolder>()
					{
						@Override
						public InternalDataSourceHolder apply(InternalDataSourceKey key)
						{
							DataSource dataSource = createInternalDataSource(key.getDriver(), key.getUrl(),
									key.getProperties());
							InternalDataSourceHolder holder = new InternalDataSourceHolder();
							holder.setDataSource(dataSource);

							return holder;
						}
					});

			// 底层数据源无法支持此驱动时将会创建一个getDataSource()为null的InternalDataSourceHolder
			if (!dataSourceHolder.hasDataSource())
			{
				connection = getConnectionWithoutInternalDataSource(driver, url, properties);

				if (LOGGER.isDebugEnabled())
					LOGGER.debug(
							"Got a connection without internal DataSource for {}, "
									+ "because the internal DataSource can not support this driver",
							key.copyOfPsdMask());
			}
			else
			{
				connection = dataSourceHolder.getDataSource().getConnection();

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Got a connection from the internal DataSource for {}", key.copyOfPsdMask());
			}
		}
		catch (Throwable t)
		{
			@JDBCCompatiblity("底层数据源可能会存在无法兼容驱动程序而抛出异常的情况（比如老版本的Hive1.2.2驱动不支持Connection.isValid()方法，而DBCP2会用到它）"
					+ "，所以在这里采取降级策略：抛弃底层数据源，改为直接新建连接。"
					+ "需要注意的是，这里的异常也可能是由其他原因导致的（比如连接参数错误等），因此，只有在直接新建连接成功之后，才能断定是底层数据源不兼容的问题。")

			boolean throwByDataSource = (dataSourceHolder != null && dataSourceHolder.hasDataSource());

			if (!throwByDataSource)
			{
				throw t;
			}
			// 如果是IO异常（网络不通时可能抛出），比如：SocketException、SocketTimeoutException，则不必再尝试
			else if (isIOExceptionCause(t))
			{
				throw t;
			}
			else
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("Get connection from the internal DataSource failed for {}, "
							+ "now try without internal DataSource", key.copyOfPsdMask(), t);
				}

				JdbcUtil.closeConnection(connection);

				try
				{
					connection = getConnectionWithoutInternalDataSource(driver, url, properties);

					InternalDataSourceHolder nonDataSourceHolder = new InternalDataSourceHolder();
					nonDataSourceHolder.setDataSource(null);
					this.internalDataSourceCache.invalidate(key);
					this.internalDataSourceCache.put(key, nonDataSourceHolder);

					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(
								"Get connection success without internal DataSource for {}, "
										+ "the internal DataSource does exactly not support this driver",
								key.copyOfPsdMask());
					}
				}
				catch (Throwable e)
				{
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(
								"Get connection fail without internal DataSource for {}, "
										+ "the internal DataSource is not sure if support this driver",
								key.copyOfPsdMask(), e);
					}

					JdbcUtil.closeConnection(connection);

					throw t;
				}
			}
		}

		return connection;
	}

	/**
	 * 指定异常链是否由{@linkplain IOException}导致。
	 * 
	 * @param t
	 * @return
	 */
	protected boolean isIOExceptionCause(Throwable t)
	{
		Throwable cause = t;

		for (int i = 0; i < 10000; i++)
		{
			if (cause == null)
				return false;

			if (cause instanceof IOException)
				return true;

			cause = cause.getCause();
		}

		return false;
	}

	protected Connection getConnectionWithoutInternalDataSource(Driver driver, String url, Properties properties)
			throws Throwable
	{
		return driver.connect(url, properties);
	}

	protected DataSource createInternalDataSource(Driver driver, String url, Properties properties)
	{
		DriverBasicDataSource re = new DriverBasicDataSource(driver, url, properties);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Create internal data source for {}", new ConnectionOption(url, properties).copyOfPsdMask());

		return re;
	}

	protected PreferedDriverEntity createPreferedDriverEntity(DriverEntityDriver driverEntityDriver)
	{
		if (driverEntityDriver == null)
		{
			return new PreferedDriverEntity(null, -1);
		}
		else
		{
			return new PreferedDriverEntity(driverEntityDriver,
					this.driverEntityManager.getLastModified(driverEntityDriver.getDriverEntity()));
		}
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
	 * @param driver
	 * @param url
	 * @param properties
	 */
	protected void processConnectionProperties(Driver driver, String url, Properties properties)
	{
		if (this.propertiesProcessor != null)
			this.propertiesProcessor.process(driver, url, properties);
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

	protected static class DriverEntityDriver
	{
		private final DriverEntity driverEntity;

		private final Driver driver;

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

		public Driver getDriver()
		{
			return driver;
		}
	}

	protected static class PreferedDriverEntity
	{
		private final DriverEntityDriver driverEntityDriver;

		private final long creationModified;

		/**
		 * 创建实例。
		 * 
		 * @param driverEntityDriver
		 *            允许{@code null}
		 */
		public PreferedDriverEntity(DriverEntityDriver driverEntityDriver, long creationModified)
		{
			super();
			this.driverEntityDriver = driverEntityDriver;
			this.creationModified = creationModified;
		}

		public boolean hasDriverEntityDriver()
		{
			return (this.driverEntityDriver != null);
		}

		public DriverEntityDriver getDriverEntityDriver()
		{
			return driverEntityDriver;
		}

		public long getCreationModified()
		{
			return creationModified;
		}
	}

	protected static class InternalDataSourceKey
	{
		private final Driver driver;
		private final String url;
		private final Properties properties;

		public InternalDataSourceKey(Driver driver, String url, Properties properties)
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

		public String getUrl()
		{
			return url;
		}

		public Properties getProperties()
		{
			return properties;
		}

		/**
		 * 复制，但是对密码脱敏处理。
		 * 
		 * @return
		 */
		public InternalDataSourceKey copyOfPsdMask()
		{
			ConnectionOption co = new ConnectionOption(this.url, this.properties);
			co = co.copyOfPsdMask();

			return new InternalDataSourceKey(this.driver, this.url, co.getProperties());
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
			InternalDataSourceKey other = (InternalDataSourceKey) obj;
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

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [driver=" + driver + ", url=" + url + ", properties=" + properties
					+ "]";
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

		private Properties connectionProperties;

		public DriverBasicDataSource(Driver driver, String url, Properties properties)
		{
			super();
			this.driver = driver;
			super.setDriverClassName(driver.getClass().getName());
			super.setUrl(url);
			this.connectionProperties = properties;
		}

		@Override
		public Driver getDriver()
		{
			return driver;
		}

		@Override
		public void setDriver(Driver driver)
		{
			this.driver = driver;
		}

		@Override
		protected ConnectionFactory createConnectionFactory() throws SQLException
		{
			return new DriverConnectionFactory(driver, getUrl(), this.connectionProperties);
		}
	}

	/**
	 * {@linkplain DriverBasicDataSource}缓存移除监听器。
	 * <p>
	 * 它负责调用{@linkplain DriverBasicDataSource#close()}。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class DriverBasicDataSourceRemovalListener
			implements RemovalListener<InternalDataSourceKey, InternalDataSourceHolder>
	{
		@Override
		public void onRemoval(@Nullable InternalDataSourceKey key, @Nullable InternalDataSourceHolder value,
				@NonNull RemovalCause cause)
		{
			if (!value.hasDataSource())
				return;

			DataSource dataSource = value.getDataSource();

			if (!(dataSource instanceof DriverBasicDataSource))
				throw new UnsupportedOperationException(
						"This " + DriverBasicDataSourceRemovalListener.class.getSimpleName() + " only support "
								+ DriverBasicDataSource.class.getSimpleName());

			try
			{
				((DriverBasicDataSource) dataSource).close();

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Close cache removed internal data source for {}", key.copyOfPsdMask());
			}
			catch (SQLException e)
			{
				LOGGER.error("Close cache removed internal data source error", e);
			}
		}
	}

	/**
	 * 内置数据源持有类。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected static class InternalDataSourceHolder
	{
		/** 内置数据源 */
		private DataSource dataSource = null;

		public InternalDataSourceHolder()
		{
			super();
		}

		/**
		 * 是否持有数据源。
		 * 
		 * @return
		 */
		public boolean hasDataSource()
		{
			return (this.dataSource != null);
		}

		/**
		 * 获取数据源实例。
		 * 
		 * @return 可能为{@code null}
		 */
		public DataSource getDataSource()
		{
			return dataSource;
		}

		public void setDataSource(DataSource dataSource)
		{
			this.dataSource = dataSource;
		}
	}
}
