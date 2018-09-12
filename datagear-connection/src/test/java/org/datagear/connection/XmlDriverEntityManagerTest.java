/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.List;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.XmlDriverEntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain XmlDriverEntityManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class XmlDriverEntityManagerTest
{
	public XmlDriverEntityManagerTest()
	{
		super();
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test() throws Exception
	{
		XmlDriverEntityManager xmlDriverEntityManager = new XmlDriverEntityManager("target/drivers/");
		xmlDriverEntityManager.init();

		DriverEntity[] expected = new DriverEntity[4];

		DriverEntity driverEntity0 = new DriverEntity("mysql", "com.mysql.Driver");
		expected[0] = driverEntity0;

		DriverEntity driverEntity1 = new DriverEntity("oracle", "oracle.jdbc.OracleDriver");
		expected[1] = driverEntity1;

		DriverEntity driverEntity2 = new DriverEntity("my", "my.jdbc.MyDriver");
		driverEntity2.setDisplayName("my");
		driverEntity2.setDisplayDesc("my-description, used for database <a>, b, c");
		expected[2] = driverEntity2;

		DriverEntity driverEntity3 = new DriverEntity("my1", "my.jdbc.MyDriver");
		driverEntity3.setDisplayDesc("my-description1");
		expected[3] = driverEntity3;

		xmlDriverEntityManager.add(expected);

		List<DriverEntity> actualList = xmlDriverEntityManager.getAll();
		DriverEntity[] actual = actualList.toArray(new DriverEntity[actualList.size()]);

		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void getDriverTest() throws Exception
	{
		printlnMyContextDrivers();

		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("src/test/resources/drivers");

		{
			Driver driver = driverEntityManager.getDriver(DriverEntity.valueOf("mysql", "com.mysql.jdbc.Driver"));
			assertNotNull(driver);
			assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());
		}

		println();

		{
			Driver driver = driverEntityManager.getDriver(DriverEntity.valueOf("oracle", "oracle.jdbc.OracleDriver"));
			assertNotNull(driver);
			assertEquals("oracle.jdbc.OracleDriver", driver.getClass().getName());
		}

		println();

		{
			Driver driver = driverEntityManager
					.getDriver(DriverEntity.valueOf("mysql-connector-java-5.1.23.jar", "com.mysql.jdbc.Driver"));
			assertNotNull(driver);
			assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());
		}

		println();

		{
			{
				Driver driver = driverEntityManager.getDriver(DriverEntity.valueOf("mixed", "com.mysql.jdbc.Driver"));
				assertNotNull(driver);
				assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());
			}

			println();

			{
				Driver driver = driverEntityManager
						.getDriver(DriverEntity.valueOf("mixed", "oracle.jdbc.OracleDriver"));
				assertNotNull(driver);
				assertEquals("oracle.jdbc.OracleDriver", driver.getClass().getName());
			}
		}

		{
			for (int i = 0; i < 5; i++)
			{
				println("----" + i + "---");

				Driver driver = driverEntityManager.getDriver(DriverEntity.valueOf("mysql", "com.mysql.jdbc.Driver"));
				assertNotNull(driver);
				assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());

				Thread.sleep(5000);
			}
		}

		println();

		driverEntityManager.releaseAllDrivers();

		printlnMyContextDrivers();
	}

	protected static void printlnMyContextDrivers()
	{
		println("");
		println("------------------");
		println("Context drivers");
		println("------------------");
		println("");

		Enumeration<Driver> drivers = DriverManager.getDrivers();

		while (drivers.hasMoreElements())
		{
			Driver ddriver = drivers.nextElement();
			ClassLoader classLoader = ddriver.getClass().getClassLoader();

			println(ddriver.getClass().getName() + ", class loader=" + classLoader);
		}

		println("");
		println("------------------");
		println("");
	}

	protected static void println()
	{
		println("");
		println("------------------");
		println("");
	}

	protected static void println(Object o)
	{
		System.out.println(o);
	}
}
