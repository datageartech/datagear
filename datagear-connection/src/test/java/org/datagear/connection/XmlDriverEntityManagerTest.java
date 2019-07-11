/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.Writer;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.datagear.util.IOUtil;
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
	public void addTest() throws Exception
	{
		{
			XmlDriverEntityManager xmlDriverEntityManager = new XmlDriverEntityManager("target/drivers/");
			xmlDriverEntityManager.init();
			xmlDriverEntityManager.releaseAll();
			xmlDriverEntityManager.getDriverEntityInfoFile().delete();
		}

		XmlDriverEntityManager xmlDriverEntityManager = new XmlDriverEntityManager("target/drivers/");
		xmlDriverEntityManager.init();

		try
		{
			DriverEntity driverEntity0 = DriverEntity.valueOf("mysql", "mysql.Driver");
			driverEntity0.setDisplayName("n0");
			driverEntity0.setDisplayDesc("d0");
			driverEntity0.setJreVersion("8");
			driverEntity0.setDatabaseName("MySQL");
			driverEntity0.setDatabaseVersions(Arrays.asList("5.5", "5.2", "5.0"));

			DriverEntity driverEntity1 = DriverEntity.valueOf("oracle", "oracle.Driver");
			driverEntity1.setDisplayName("n0");
			driverEntity1.setDisplayDesc("d0");
			driverEntity1.setJreVersion("6");
			driverEntity1.setDatabaseName("Oracle");
			driverEntity1.setDatabaseVersions(Arrays.asList("18c", "11g", "10g"));

			xmlDriverEntityManager.add(driverEntity0, driverEntity1);

			Assert.assertEquals(2, xmlDriverEntityManager.getAll().size());

			DriverEntity actual0 = xmlDriverEntityManager.get("mysql");

			Assert.assertEquals(driverEntity0.getId(), actual0.getId());
			Assert.assertEquals(driverEntity0.getDriverClassName(), actual0.getDriverClassName());
			Assert.assertEquals(driverEntity0.getDisplayName(), actual0.getDisplayName());
			Assert.assertEquals(driverEntity0.getDisplayDesc(), actual0.getDisplayDesc());
			Assert.assertEquals(driverEntity0.getJreVersion(), actual0.getJreVersion());
			Assert.assertEquals(driverEntity0.getDatabaseName(), actual0.getDatabaseName());
			Assert.assertEquals(driverEntity0.getDatabaseVersions(), actual0.getDatabaseVersions());

			DriverEntity actual1 = xmlDriverEntityManager.get("oracle");

			Assert.assertEquals(driverEntity1.getId(), actual1.getId());
			Assert.assertEquals(driverEntity1.getDriverClassName(), actual1.getDriverClassName());
			Assert.assertEquals(driverEntity1.getDisplayName(), actual1.getDisplayName());
			Assert.assertEquals(driverEntity1.getDisplayDesc(), actual1.getDisplayDesc());
			Assert.assertEquals(driverEntity1.getJreVersion(), actual1.getJreVersion());
			Assert.assertEquals(driverEntity1.getDatabaseName(), actual1.getDatabaseName());
			Assert.assertEquals(driverEntity1.getDatabaseVersions(), actual1.getDatabaseVersions());
		}
		finally
		{
			xmlDriverEntityManager.releaseAll();
			xmlDriverEntityManager.getDriverEntityInfoFile().delete();
		}
	}

	@Test
	public void getAllTest() throws Exception
	{
		{
			XmlDriverEntityManager xmlDriverEntityManager = new XmlDriverEntityManager("target/drivers/");
			xmlDriverEntityManager.init();
			xmlDriverEntityManager.releaseAll();
			xmlDriverEntityManager.getDriverEntityInfoFile().delete();
		}

		XmlDriverEntityManager xmlDriverEntityManager = new XmlDriverEntityManager("target/drivers/");
		xmlDriverEntityManager.init();

		try
		{
			DriverEntity[] expected = new DriverEntity[5];

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

			DriverEntity driverEntity4 = new DriverEntity("mysql-jre8", "com.mysql.cj.Driver");
			expected[4] = driverEntity4;

			xmlDriverEntityManager.add(expected);

			List<DriverEntity> actualList = xmlDriverEntityManager.getAll();
			DriverEntity[] actual = actualList.toArray(new DriverEntity[actualList.size()]);

			Assert.assertArrayEquals(expected, actual);
		}
		finally
		{
			xmlDriverEntityManager.releaseAll();

			xmlDriverEntityManager.getDriverEntityInfoFile().delete();
		}
	}

	@Test
	public void getDriverTest() throws Exception
	{
		printlnMyContextDrivers();

		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("src/test/resources/drivers");

		try
		{
			{
				Driver driver = driverEntityManager.getDriver(DriverEntity.valueOf("mysql", "com.mysql.jdbc.Driver"));
				assertNotNull(driver);
				assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());
			}

			println();

			{
				try
				{
					Driver driver = driverEntityManager
							.getDriver(DriverEntity.valueOf("mysql-jre8", "com.mysql.cj.jdbc.Driver"));

					assertNotNull(driver);
					assertEquals("com.mysql.cj.jdbc.Driver", driver.getClass().getName());
				}
				catch (PathDriverFactoryException e)
				{
					// 如果当前JRE小于8，将会抛出此异常
					assertEquals(DriverClassFormatErrorException.class, e.getClass());
				}
			}

			println();

			{
				Driver driver = driverEntityManager
						.getDriver(DriverEntity.valueOf("oracle", "oracle.jdbc.OracleDriver"));
				assertNotNull(driver);
				assertEquals("oracle.jdbc.OracleDriver", driver.getClass().getName());
			}

			println();

			{
				{
					Driver driver = driverEntityManager
							.getDriver(DriverEntity.valueOf("mixed", "com.mysql.jdbc.Driver"));
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

			println();

			printlnMyContextDrivers();
		}
		finally
		{
			driverEntityManager.releaseAll();
		}
	}

	@Test
	public void getDriverTestWithDirectoryModified() throws Exception
	{
		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("src/test/resources/drivers");

		try
		{
			DriverEntity driverEntity = DriverEntity.valueOf("mysql", "com.mysql.jdbc.Driver");

			Driver driver = driverEntityManager.getDriver(driverEntity);
			ClassLoader classLoader = driver.getClass().getClassLoader();

			assertEquals("com.mysql.jdbc.Driver", driver.getClass().getName());

			Driver unmodifiedDriver = driverEntityManager.getDriver(driverEntity);
			ClassLoader unmodifiedClassLoader = unmodifiedDriver.getClass().getClassLoader();

			assertEquals("com.mysql.jdbc.Driver", unmodifiedDriver.getClass().getName());

			assertEquals(classLoader, unmodifiedClassLoader);

			File modifiedFile = new File(driverEntityManager.getRootDirectory(), "mysql/modified.txt");
			Writer writer = null;
			try
			{
				writer = IOUtil.getWriter(modifiedFile);
				writer.write("modified");
			}
			finally
			{
				writer.close();
			}

			Driver modifiedDriver = driverEntityManager.getDriver(driverEntity);
			ClassLoader modifiedClassLoader = modifiedDriver.getClass().getClassLoader();

			assertEquals("com.mysql.jdbc.Driver", modifiedDriver.getClass().getName());

			assertNotEquals(classLoader, modifiedClassLoader);

			modifiedFile.delete();
		}
		finally
		{
			driverEntityManager.releaseAll();
		}
	}

	@Test
	public void exportToZipTest() throws Exception
	{
		String zipFilePath = "target/exportToZipTest.zip";
		String zipFilePathFiltered = "target/exportToZipTestFiltered.zip";

		new File(zipFilePath).delete();

		List<DriverEntity> expected = null;
		List<DriverEntity> expectedFiltered = new ArrayList<DriverEntity>();

		{
			XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("src/test/resources/drivers");

			driverEntityManager.init();

			expected = driverEntityManager.getAll();

			{
				ZipOutputStream out = IOUtil.getZipOutputStream(zipFilePath);
				driverEntityManager.exportToZip(out);
				IOUtil.close(out);

				Assert.assertTrue(new File(zipFilePath).exists());
			}

			{
				ZipOutputStream out = IOUtil.getZipOutputStream(zipFilePathFiltered);
				driverEntityManager.exportToZip(out, "mysql", "oracle");
				IOUtil.close(out);

				Assert.assertTrue(new File(zipFilePathFiltered).exists());

				expectedFiltered.add(driverEntityManager.get("mysql"));
				expectedFiltered.add(driverEntityManager.get("oracle"));
			}
		}

		{
			XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("target/exportToZipTest");
			driverEntityManager.init();

			ZipInputStream in = IOUtil.getZipInputStream(zipFilePath);
			driverEntityManager.importFromZip(in);
			IOUtil.close(in);

			Assert.assertEquals(expected, driverEntityManager.getAll());
		}

		{
			XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("target/exportToZipTestFiltered");
			driverEntityManager.init();

			ZipInputStream in = IOUtil.getZipInputStream(zipFilePathFiltered);
			driverEntityManager.importFromZip(in);
			IOUtil.close(in);

			Assert.assertEquals(expectedFiltered, driverEntityManager.getAll());
		}
	}

	@Test
	public void readDriverEntitiesFromZipTest() throws Exception
	{
		String zipFilePath = "target/readDriverEntitiesFromZip.zip";

		new File(zipFilePath).delete();

		List<DriverEntity> expected = null;

		{
			XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("src/test/resources/drivers");

			driverEntityManager.init();

			expected = driverEntityManager.getAll();

			{
				ZipOutputStream out = IOUtil.getZipOutputStream(zipFilePath);
				driverEntityManager.exportToZip(out);
				IOUtil.close(out);
			}
		}

		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager("target/readDriverEntitiesFromZip");
		driverEntityManager.init();

		ZipInputStream in = IOUtil.getZipInputStream(zipFilePath);
		List<DriverEntity> driverEntities = driverEntityManager.readDriverEntitiesFromZip(in);
		IOUtil.close(in);

		Assert.assertEquals(expected, driverEntities);
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
