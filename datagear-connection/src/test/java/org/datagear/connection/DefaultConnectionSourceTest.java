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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Driver;
import java.util.concurrent.ConcurrentMap;

import org.datagear.connection.DefaultConnectionSource.DriverBasicDataSource;
import org.datagear.connection.DefaultConnectionSource.InternalDataSourceHolder;
import org.datagear.connection.DefaultConnectionSource.InternalDataSourceKey;
import org.datagear.connection.DefaultConnectionSource.PreferedDriverEntity;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.test.DBTestSupport;
import org.junit.Test;

/**
 * {@linkplain DefaultConnectionSource}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultConnectionSourceTest extends DBTestSupport
{
	@Test
	public void getConnectionTest_DriverEntity_ConnectionOption() throws Exception
	{
		int expiredSeconds = 3;

		XmlDriverEntityManager driverEntityManager = createDriverEntityManager("1");
		DriverEntity driverEntity = getDriverEntity();
		DriverEntity noneDriverEntity = getNoneDriverEntity();
		ConnectionOption option = ConnectionOption.valueOf(getUrl(), getUser(), getPassword());

		TestDefaultConnectionSource cs = new TestDefaultConnectionSource(driverEntityManager, expiredSeconds, 5);
		ConcurrentMap<InternalDataSourceKey, InternalDataSourceHolder> map = cs.getInternalDataSourceCache().asMap();

		try
		{
			// 驱动不存在
			{
				assertThrows(DriverNotFoundException.class, () ->
				{
					cs.getConnection(noneDriverEntity, option);
				});
			}

			Driver driver = driverEntityManager.getDriver(driverEntity);
			InternalDataSourceKey key = new InternalDataSourceKey(driver, option.getUrl(), option.getProperties());
			InternalDataSourceHolder dh = map.get(key);

			assertTrue(map.isEmpty());
			assertNull(dh);

			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(driverEntity, option);

					assertEquals(1, map.size());
					assertNotNull(cn);
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			// 多次取同一连接，应只有一个内部数据源
			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(driverEntity, option);

					assertEquals(1, map.size());
					assertNotNull(cn);
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}
			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(driverEntity, option);

					assertEquals(1, map.size());
					assertNotNull(cn);
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			// 超时后应关闭内部数据源
			{
				dh = map.get(key);
				DriverBasicDataSource dbds = (dh == null ? null : (DriverBasicDataSource) dh.getDataSource());

				assertNotNull(dh);
				assertNotNull(dbds);
				assertFalse(dbds.isClosed());

				Thread.sleep((expiredSeconds + 2) * 1000);

				assertTrue(dbds.isClosed());
				assertTrue(map.isEmpty());
			}
		}
		finally
		{
			IOUtil.close(cs);
			driverEntityManager.releaseAll();
		}
	}

	@Test
	public void getConnectionTest_ConnectionOption() throws Exception
	{
		int expiredSeconds = 3;

		XmlDriverEntityManager driverEntityManager = createDriverEntityManager("2");
		DriverEntity driverEntity = getDriverEntity();
		ConnectionOption option = ConnectionOption.valueOf(getUrl(), getUser(), getPassword());

		TestDefaultConnectionSource cs = new TestDefaultConnectionSource(driverEntityManager, expiredSeconds, 5);
		ConcurrentMap<InternalDataSourceKey, InternalDataSourceHolder> internalDsMap = cs.getInternalDataSourceCache()
				.asMap();
		ConcurrentMap<String, PreferedDriverEntity> urlPreferedDriverEntities = cs.getUrlPreferedDriverEntities();

		try
		{
			assertTrue(urlPreferedDriverEntities.isEmpty());

			// 驱动不存在
			{
				ConnectionOption noneOption = ConnectionOption.valueOf("jdbc:none://none", getUser(), getPassword());

				assertThrows(UnsupportedGetConnectionException.class, () ->
				{
					cs.getConnection(noneOption);
				});

				assertThrows(UnsupportedGetConnectionException.class, () ->
				{
					cs.getConnection(noneOption);
				});

				assertEquals(1, urlPreferedDriverEntities.size());
			}

			Driver driver = driverEntityManager.getDriver(driverEntity);
			InternalDataSourceKey key = new InternalDataSourceKey(driver, option.getUrl(), option.getProperties());
			InternalDataSourceHolder dsh = internalDsMap.get(key);

			assertTrue(internalDsMap.isEmpty());
			assertNull(dsh);

			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(option);

					assertNotNull(cn);
					assertEquals(1, internalDsMap.size());
					assertEquals(2, urlPreferedDriverEntities.size());
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			// 多次取同一连接，应只有一个内部数据源
			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(option);

					assertNotNull(cn);
					assertEquals(1, internalDsMap.size());
					assertEquals(2, urlPreferedDriverEntities.size());
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}
			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(option);

					assertNotNull(cn);
					assertEquals(1, internalDsMap.size());
					assertEquals(2, urlPreferedDriverEntities.size());
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			// 超时后应关闭内部数据源
			{
				dsh = internalDsMap.get(key);
				DriverBasicDataSource dbds = (dsh == null ? null : (DriverBasicDataSource) dsh.getDataSource());

				assertNotNull(dsh);
				assertNotNull(dbds);
				assertFalse(dbds.isClosed());

				Thread.sleep((expiredSeconds + 2) * 1000);

				assertTrue(dbds.isClosed());
				assertTrue(internalDsMap.isEmpty());
			}
		}
		finally
		{
			IOUtil.close(cs);
			driverEntityManager.releaseAll();
		}
	}

	@Test
	public void getConnectionTest_ConnectionOption_driver_modified() throws Exception
	{
		int expiredSeconds = 6;

		XmlDriverEntityManager driverEntityManager = createDriverEntityManager("3");
		DriverEntity driverEntity = getDriverEntity();
		ConnectionOption option = ConnectionOption.valueOf(getUrl(), getUser(), getPassword());

		TestDefaultConnectionSource cs = new TestDefaultConnectionSource(driverEntityManager, expiredSeconds, 5);
		ConcurrentMap<InternalDataSourceKey, InternalDataSourceHolder> internalDsMap = cs.getInternalDataSourceCache()
				.asMap();
		ConcurrentMap<String, PreferedDriverEntity> urlPreferedDriverEntities = cs.getUrlPreferedDriverEntities();

		try
		{
			assertTrue(urlPreferedDriverEntities.isEmpty());

			Driver driver1 = driverEntityManager.getDriver(driverEntity);
			InternalDataSourceKey key1 = new InternalDataSourceKey(driver1, option.getUrl(), option.getProperties());
			InternalDataSourceHolder dsh1 = internalDsMap.get(key1);

			assertTrue(internalDsMap.isEmpty());
			assertNull(dsh1);

			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(option);

					assertNotNull(cn);
					assertEquals(1, internalDsMap.size());
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			assertEquals(1, urlPreferedDriverEntities.size());
			assertEquals(driver1, urlPreferedDriverEntities.get(getUrl()).getDriverEntityDriver().getDriver());

			Thread.sleep(2000);

			File modifiedFile = FileUtil.getFile(driverEntityManager.getRootDirectory(), "mysql-jre8/modified.txt");
			Writer writer = null;
			try
			{
				writer = IOUtil.getWriter(modifiedFile);
				writer.write("modified");
			}
			finally
			{
				IOUtil.close(writer);
			}

			Thread.sleep(2000);

			Driver driver2 = driverEntityManager.getDriver(driverEntity);
			InternalDataSourceKey key2 = new InternalDataSourceKey(driver2, option.getUrl(), option.getProperties());
			InternalDataSourceHolder dsh2 = internalDsMap.get(key2);

			assertNotEquals(driver1, driver2);
			assertNull(dsh2);

			{
				Connection cn = null;

				try
				{
					cn = cs.getConnection(option);

					assertNotNull(cn);
					assertEquals(2, internalDsMap.size());
				}
				finally
				{
					JdbcUtil.closeConnection(cn);
				}
			}

			assertEquals(1, urlPreferedDriverEntities.size());
			assertEquals(driver2, urlPreferedDriverEntities.get(getUrl()).getDriverEntityDriver().getDriver());

			// 超时后应关闭内部数据源
			{
				dsh1 = internalDsMap.get(key1);
				DriverBasicDataSource dbds1 = (dsh1 == null ? null : (DriverBasicDataSource) dsh1.getDataSource());

				dsh2 = internalDsMap.get(key2);
				DriverBasicDataSource dbds2 = (dsh2 == null ? null : (DriverBasicDataSource) dsh2.getDataSource());

				assertNotNull(dsh1);
				assertNotNull(dbds1);
				assertFalse(dbds1.isClosed());

				assertNotNull(dsh2);
				assertNotNull(dbds2);
				assertFalse(dbds2.isClosed());

				Thread.sleep((expiredSeconds + 2) * 1000);

				assertTrue(dbds1.isClosed());
				assertTrue(dbds2.isClosed());
				assertTrue(internalDsMap.isEmpty());
			}
		}
		finally
		{
			IOUtil.close(cs);
			driverEntityManager.releaseAll();
		}
	}

	protected XmlDriverEntityManager createDriverEntityManager(String name) throws Exception
	{
		File directory = FileUtil.getDirectory("target/test/DefaultConnectionSourceTest/" + name);
		FileUtil.clearDirectory(directory);

		IOUtil.copyInto(FileUtil.getFile("src/test/resources/drivers/mysql-jre8"), directory);
		IOUtil.copyInto(FileUtil.getFile("src/test/resources/drivers/driverEntityInfo.xml"), directory);

		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager(directory);
		driverEntityManager.init();

		return driverEntityManager;
	}

	protected DriverEntity getDriverEntity()
	{
		return DriverEntity.valueOf("mysql-jre8", "com.mysql.cj.jdbc.Driver");
	}

	protected DriverEntity getNoneDriverEntity()
	{
		return DriverEntity.valueOf("none", "com.mysql.cj.jdbc.Driver");
	}

	protected static class TestDefaultConnectionSource extends DefaultConnectionSource
	{
		public TestDefaultConnectionSource(DriverEntityManager driverEntityManager, Integer internalDsExpiredSeconds,
				Integer maxCacheSize)
		{
			super(driverEntityManager, internalDsExpiredSeconds, maxCacheSize);
		}
	}
}
