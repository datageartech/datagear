/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.JdbcUtil;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfo;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.ExportedKeyInfo;
import org.datagear.dbinfo.GenericDatabaseInfoResolver;
import org.datagear.dbinfo.ImportedKeyInfo;
import org.datagear.dbinfo.TableInfo;
import org.datagear.dbinfo.WildcardDevotedDatabaseInfoResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain MysqlDatabaseInfoResolver}单元测试。
 * 
 * @author datagear@163.com
 *
 */
public class GenericDatabaseInfoResolverTest extends DatabaseTestSupport
{
	private GenericDatabaseInfoResolver genericDatabaseInfoResolver;

	public GenericDatabaseInfoResolverTest()
	{
		super();

		List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolver = new ArrayList<DevotedDatabaseInfoResolver>();
		devotedDatabaseInfoResolver.add(new WildcardDevotedDatabaseInfoResolver());
		this.genericDatabaseInfoResolver = new GenericDatabaseInfoResolver(devotedDatabaseInfoResolver);
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
	public void getDatabaseInfoTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		DatabaseInfo databaseInfo = null;

		try
		{
			databaseInfo = this.genericDatabaseInfoResolver.getDatabaseInfo(cn);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		Assert.assertEquals("MySQL", databaseInfo.getProductName());

		println(databaseInfo);
	}

	@Test
	public void getTableInfosTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		TableInfo[] tableInfos = null;

		try
		{
			tableInfos = this.genericDatabaseInfoResolver.getTableInfos(cn);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		for (int i = 0; i < tableInfos.length; i++)
		{
			println(tableInfos[i]);
		}
	}

	@Test
	public void getTableInfoTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		TableInfo tableInfo = null;

		try
		{
			tableInfo = this.genericDatabaseInfoResolver.getTableInfo(cn, "T_ORDER");
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		println(tableInfo);
	}

	@Test
	public void getColumnInfosTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		ColumnInfo[] columnInfos = null;

		try
		{
			columnInfos = this.genericDatabaseInfoResolver.getColumnInfos(cn, "T_ACCOUNT");
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		for (int i = 0; i < columnInfos.length; i++)
		{
			println(columnInfos[i]);
		}
	}

	@Test
	public void getPrimaryKeyColumnNamesTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		String[] primaryKeyColumnNames = null;

		try
		{
			primaryKeyColumnNames = this.genericDatabaseInfoResolver.getPrimaryKeyColumnNames(cn, "T_ORDER");
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		Assert.assertArrayEquals(new String[] { "ID" }, primaryKeyColumnNames);

		for (int i = 0; i < primaryKeyColumnNames.length; i++)
		{
			println(primaryKeyColumnNames[i]);
		}
	}

	@Test
	public void getImportedKeyInfosTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		ImportedKeyInfo[] importedKeyInfos = null;

		try
		{
			importedKeyInfos = this.genericDatabaseInfoResolver.getImportedKeyInfos(cn, "T_ORDER");
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		// TODO assert

		for (int i = 0; i < importedKeyInfos.length; i++)
		{
			println(importedKeyInfos[i]);
		}
	}

	@Test
	public void getExportedKeyInfosTest() throws Exception
	{
		Connection cn = getMysqlConnection();

		try
		{
			{
				ExportedKeyInfo[] exportedKeyInfos = null;
				exportedKeyInfos = this.genericDatabaseInfoResolver.getExportedKeyInfos(cn, "T_ORDER");

				// TODO assert

				println();
				println("T_ORDER");
				for (int i = 0; i < exportedKeyInfos.length; i++)
				{
					println(exportedKeyInfos[i]);
				}
			}

			{
				ExportedKeyInfo[] exportedKeyInfos = null;
				exportedKeyInfos = this.genericDatabaseInfoResolver.getExportedKeyInfos(cn, "T_ACCOUNT");

				// TODO assert

				println();
				println("T_ACCOUNT");
				for (int i = 0; i < exportedKeyInfos.length; i++)
				{
					println(exportedKeyInfos[i]);
				}
			}
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}
}
