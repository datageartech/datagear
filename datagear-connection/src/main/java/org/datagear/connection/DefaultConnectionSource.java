/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

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

	private Cache<ConnectionIdentity, InternalDataSourceHolder> internalDataSourceCache;

	private ConcurrentMap<String, PreferedDriverEntityResult> _urlPreferedDriverEntityMap = new ConcurrentHashMap<>();

	private volatile long _driverEntityManagerLastModified = -1;

	public DefaultConnectionSource()
	{
		this(null);
	}

	public DefaultConnectionSource(DriverEntityManager driverEntityManager)
	{
		super();
		this.driverEntityManager = driverEntityManager;
		this.internalDataSourceCache = CacheBuilder.newBuilder().maximumSize(50)
				.expireAfterAccess(60 * 24, TimeUnit.MINUTES)
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

	protected Cache<ConnectionIdentity, InternalDataSourceHolder> getInternalDataSourceCache()
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
			Cache<ConnectionIdentity, InternalDataSourceHolder> internalDataSourceCache)
	{
		this.internalDataSourceCache = internalDataSourceCache;
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
		if (this.driverEntityManager.getLastModified() > this._driverEntityManagerLastModified)
		{
			this._driverEntityManagerLastModified = this.driverEntityManager.getLastModified();
			this._urlPreferedDriverEntityMap.clear();
		}

		String url = connectionOption.getUrl();

		PreferedDriverEntityResult preferedDriverEntityResult = this._urlPreferedDriverEntityMap.get(url);

		if (preferedDriverEntityResult != null)
		{
			if (preferedDriverEntityResult.hasDriverEntity())
			{
				DriverEntity preferedDriverEntity = preferedDriverEntityResult.getDriverEntity();

				Driver preferedDriver = this.driverEntityManager.getDriver(preferedDriverEntity);
				Connection preferedConnection = getConnection(preferedDriver, connectionOption);

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Get prefered connection by cached [" + preferedDriverEntity + "] for ["
							+ connectionOption + "]");

				return preferedConnection;
			}
			else
			{
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Get null connection by cached no-prefered " + DriverEntity.class.getSimpleName()
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

				this._urlPreferedDriverEntityMap.put(connectionOption.getUrl(),
						new PreferedDriverEntityResult(driverEntity));

				break;
			}
			catch (ConnectionSourceException e)
			{
				if (i == len - 1)
				{
					// 使用最后一个最为首选，这样下次获取时，可以使用缓存中的它，直接抛出异常供上层应用知晓，不用再查找一次
					this._urlPreferedDriverEntityMap.put(url, new PreferedDriverEntityResult(driverEntity));

					// 抛出最后一个异常，供上层应用知晓
					throw e;
				}
				else
				{
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Get connection with [" + driverEntity + "]  for [" + connectionOption + "] error",
								e);
				}
			}
		}

		if (preferedConnection == null)
		{
			this._urlPreferedDriverEntityMap.put(url, new PreferedDriverEntityResult());

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
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Check if url accepted with [" + driverEntity
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
						if (LOGGER.isErrorEnabled())
							LOGGER.error("Check if [" + driverEntity
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
		ConnectionIdentity connectionIdentity = ConnectionIdentity.valueOf(url, properties);

		Connection connection = null;
		InternalDataSourceHolder dataSourceHolder = null;

		try
		{
			dataSourceHolder = this.internalDataSourceCache.get(connectionIdentity,
					new Callable<InternalDataSourceHolder>()
					{
						@Override
						public InternalDataSourceHolder call() throws Exception
						{
							DataSource dataSource = createInternalDataSource(driver, url, properties);
							InternalDataSourceHolder holder = new InternalDataSourceHolder();
							holder.setDataSource(dataSource);

							return holder;
						}
					});

			// 底层数据源无法支持此驱动时将会创建一个getDataSource()为null的InternalDataSourceHolder
			if (!dataSourceHolder.hasDataSource())
			{
				connection = getConnectionWithoutInternalDataSource(driver, url, properties);

				LOGGER.debug("Got a connection without internal DataSource for {}, "
						+ "because the internal DataSource can not support this driver", connectionIdentity);
			}
			else
			{
				connection = dataSourceHolder.getDataSource().getConnection();

				LOGGER.debug("Got a connection from the internal DataSource for {}", connectionIdentity);
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
			else
			{
				LOGGER.debug("Get connection from the internal DataSource failed for {}, "
						+ "now try without internal DataSource", connectionIdentity, t);

				JdbcUtil.closeConnection(connection);

				try
				{
					connection = getConnectionWithoutInternalDataSource(driver, url, properties);

					InternalDataSourceHolder nonDataSourceHolder = new InternalDataSourceHolder();
					nonDataSourceHolder.setDataSource(null);
					this.internalDataSourceCache.invalidate(connectionIdentity);
					this.internalDataSourceCache.put(connectionIdentity, nonDataSourceHolder);

					LOGGER.debug(
							"Get connection success without internal DataSource for {}, "
									+ "the internal DataSource does exactly not support this driver",
							connectionIdentity);
				}
				catch (Throwable e)
				{
					LOGGER.debug(
							"Get connection fail without internal DataSource for {}, "
									+ "the internal DataSource is not sure if support this driver",
							connectionIdentity, e);

					JdbcUtil.closeConnection(connection);

					throw t;
				}
			}
		}

		return connection;
	}

	protected Connection getConnectionWithoutInternalDataSource(Driver driver, String url, Properties properties)
			throws Throwable
	{
		return driver.connect(url, properties);
	}

	protected DataSource createInternalDataSource(Driver driver, String url, Properties properties)
	{
		DriverBasicDataSource re = new DriverBasicDataSource(driver, url, properties);

		LOGGER.debug("Create internal data source for {}", ConnectionIdentity.valueOf(url, properties));

		return re;
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
			implements RemovalListener<ConnectionIdentity, InternalDataSourceHolder>
	{
		@Override
		public void onRemoval(RemovalNotification<ConnectionIdentity, InternalDataSourceHolder> notification)
		{
			InternalDataSourceHolder holder = notification.getValue();

			if (!holder.hasDataSource())
				return;

			DataSource dataSource = holder.getDataSource();

			if (!(dataSource instanceof DriverBasicDataSource))
				throw new UnsupportedOperationException(
						"This " + DriverBasicDataSourceRemovalListener.class.getSimpleName() + " only support "
								+ DriverBasicDataSource.class.getSimpleName());

			try
			{
				((DriverBasicDataSource) dataSource).close();

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Close internal data source for {}", notification.getKey());
			}
			catch (SQLException e)
			{
				LOGGER.error("Close internal data source exception:", e);
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
