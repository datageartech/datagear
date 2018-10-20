/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain DatabaseMetaData}测试。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseMetaDataTest extends TestSupport
{
	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void getIdentifierQuoteStringTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			println("getIdentifierQuoteStringTest : ");

			println(metaData.getIdentifierQuoteString());

			println("");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getCatalogsTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getCatalogs();

			printlnResultSet(rs, "getCatalogs");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getSchemasTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getSchemas();

			printlnResultSet(rs, "getSchemas");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getUserNameTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			String username = metaData.getUserName();

			println("getUserNameTest : ");

			println(username);
			println("");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getTableTypesTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getTableTypes();

			printlnResultSet(rs, "getTableTypes");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getTablesTest() throws Exception
	{
		Properties props = new Properties();
		props.setProperty("useInformationSchema", "true");

		Connection cn = getMysqlConnection(props);

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getTables(cn.getCatalog(), null, "%", new String[] { "TABLE" });

			printlnResultSet(rs, "getTables");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getPrimaryKeysTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getPrimaryKeys(cn.getCatalog(), null, "T_ACCOUNT");

			printlnResultSet(rs, "getPrimaryKeys");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getColumnTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getColumns(cn.getCatalog(), null, "%", "%");

			printlnResultSet(rs, "getColumns");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getExportedKeysTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getExportedKeys(cn.getCatalog(), null, "T_ACCOUNT");

			printlnResultSet(rs, "getExportedKeys");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getImportedKeysTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getImportedKeys(cn.getCatalog(), null, "T_ORDER");

			printlnResultSet(rs, "getImportedKeys");
		}
		finally
		{
			cn.close();
		}
	}

	@Test
	public void getUniqueKeysTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getIndexInfo(cn.getCatalog(), null, "T_ADDRESS", true, false);

			printlnResultSet(rs, "getUniqueKeys");
		}
		finally
		{
			cn.close();
		}
	}

	/**
	 * 打印{@linkplain ResultSet}。
	 * 
	 * @param rs
	 * @param label
	 * @throws SQLException
	 */
	protected void printlnResultSet(ResultSet rs, String label) throws SQLException
	{
		ResultSetMetaData rsMeta = rs.getMetaData();
		int colCount = rsMeta.getColumnCount();

		println("---" + label + "--------------------------------------------");

		for (int i = 1; i <= colCount; i++)
		{
			print(rsMeta.getColumnLabel(i) + "(" + rsMeta.getColumnType(i) + ", " + rsMeta.getColumnTypeName(i) + ", "
					+ rsMeta.getColumnClassName(i) + "), ");
		}
		println("");

		for (int i = 1; i <= colCount; i++)
		{
			print(rsMeta.getColumnLabel(i) + ", ");
		}
		println("");

		while (rs.next())
		{
			for (int i = 1; i <= colCount; i++)
			{
				print(rs.getObject(i) + ", ");
			}
			println("");
		}

		println("---" + label + "--------------------------------------------");
		println("");
	}

	protected Connection getMysqlConnection(Properties properties) throws SQLException
	{
		properties.setProperty("user", "root");
		properties.setProperty("password", "");

		return DriverManager.getConnection(
				"jdbc:mysql://127.0.0.1:3306/datagear?useUnicode=true&amp;characterEncoding=UTF-8", properties);
	}
}
