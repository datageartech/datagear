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

package org.datagear.meta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.ArrayMatching.arrayContaining;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.datagear.meta.resolver.DbTableTypeSpec;
import org.datagear.meta.resolver.DefaultTableTypeResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.test.DBTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain GenericDBMetaResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class GenericDBMetaResolverTest extends DBTestSupport
{
	private GenericDBMetaResolver genericDBMetaResolver;

	private Connection connection;

	public GenericDBMetaResolverTest()
	{
		super();
		this.genericDBMetaResolver = new GenericDBMetaResolver();
	}

	@Before
	public void init() throws Exception
	{
		this.connection = getConnection();
	}

	@After
	public void destroy()
	{
		JdbcUtil.closeConnection(this.connection);
	}

	@Test
	public void getTablesTest() throws Exception
	{
		List<SimpleTable> simpleTables = this.genericDBMetaResolver.getTables(this.connection);

		assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
		assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
	}

	@Test
	public void getDataTablesTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			GenericDBMetaResolver resolver = new GenericDBMetaResolver();

			List<SimpleTable> simpleTables = resolver.getDataTables(this.connection);

			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs
					.add(new DbTableTypeSpec("oracle", Collections.emptyList(), Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Collections.emptyList(), Arrays.asList("TABLE")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			List<SimpleTable> simpleTables = resolver.getDataTables(this.connection);

			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
		}
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("oracle", Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Arrays.asList("none")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			List<SimpleTable> simpleTables = resolver.getDataTables(this.connection);

			assertTrue(simpleTables.isEmpty());
		}
	}

	@Test
	public void getEntityTablesTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			GenericDBMetaResolver resolver = new GenericDBMetaResolver();

			List<SimpleTable> simpleTables = resolver.getEntityTables(this.connection);

			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs
					.add(new DbTableTypeSpec("oracle", Collections.emptyList(), Collections.emptyList(),
							Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("TABLE")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			List<SimpleTable> simpleTables = resolver.getEntityTables(this.connection);

			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
			assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
		}
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs
					.add(new DbTableTypeSpec("oracle", Collections.emptyList(), Collections.emptyList(),
							Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("none")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			List<SimpleTable> simpleTables = resolver.getEntityTables(this.connection);

			assertTrue(simpleTables.isEmpty());
		}
	}

	@Test
	public void getTableTest() throws Exception
	{
		{
			Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");
			assertThat(table, hasProperty("name", equalToIgnoringCase("T_ACCOUNT")));
			assertThat(table.getColumns(), hasItemInArray(hasProperty("name", equalToIgnoringCase("ID"))));
			assertThat(table.getPrimaryKey(), hasProperty("columnNames", hasItemInArray(equalToIgnoringCase("ID"))));
		}

		{
			Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ADDRESS");
			assertThat(table, hasProperty("name", equalToIgnoringCase("T_ADDRESS")));
			assertThat(table.getColumns(), hasItemInArray(hasProperty("name", equalToIgnoringCase("ACCOUNT_ID"))));
			assertThat(table.getUniqueKeys(),
					hasItemInArray(hasProperty("columnNames", arrayContaining(equalToIgnoringCase("ACCOUNT_ID")))));
		}
	}

	@Test
	public void getColumnsTest() throws Exception
	{
		Column[] columns = this.genericDBMetaResolver.getColumns(this.connection, "T_ACCOUNT");

		assertEquals(4, columns.length);
		assertThat(columns[0].getName(), equalToIgnoringCase("ID"));
		assertThat(columns[1].getName(), equalToIgnoringCase("NAME"));
		assertThat(columns[2].getName(), equalToIgnoringCase("HEAD_IMG"));
		assertThat(columns[3].getName(), equalToIgnoringCase("INTRODUCTION"));
	}

	@Test
	public void getTableTypesTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			GenericDBMetaResolver resolver = new GenericDBMetaResolver();

			String[] types = resolver.getTableTypes(connection);

			assertNotNull(types);

			List<String> typeList = Arrays.stream(types).map((t) -> t.toUpperCase()).collect(Collectors.toList());

			assertTrue(typeList.contains("TABLE"));
			assertTrue(typeList.contains("VIEW"));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("oracle", Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Arrays.asList("TABLE")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			String[] types = resolver.getTableTypes(connection);

			assertNotNull(types);

			List<String> typeList = Arrays.stream(types).map((t) -> t.toUpperCase()).collect(Collectors.toList());

			assertEquals(1, typeList.size());
			assertTrue(typeList.contains("TABLE"));
		}
	}

	@Test
	public void isDataTableTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			GenericDBMetaResolver resolver = new GenericDBMetaResolver();

			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "table")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", null)));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "unknown")));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("oracle", Collections.emptyList(), Arrays.asList("*view*")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("mysql", Collections.emptyList(),
					Arrays.asList("*table*", "null", "*unknown*")));
			tableTypeResolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			GenericDBMetaResolver resolver = new GenericDBMetaResolver(tableTypeResolver);

			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "table")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "A-TABLE-B")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", null)));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "unknown")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "view")));
		}
	}
}
