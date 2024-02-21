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
import org.datagear.util.test.DBTestSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain DefaultTableTypeResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultTableTypeResolverTest extends DBTestSupport
{
	private Connection connection;

	public DefaultTableTypeResolverTest()
	{
		super();
	}

	@Before
	public void init() throws Exception
	{
		this.connection = getConnection();
	}

	@Test
	public void getTableTypesTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			String[] types = resolver.getTableTypes(connection);

			assertNotNull(types);

			List<String> typeList = Arrays.stream(types).map((t) -> t.toUpperCase()).collect(Collectors.toList());

			assertTrue(typeList.contains("TABLE"));
			assertTrue(typeList.contains("VIEW"));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Arrays.asList("TABLE", "VIEW")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Arrays.asList("TABLE")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

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
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "table")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "TABLE")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "view")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "VIEW")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "alias")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "AliAs")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "synonym")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "SYnonyM")));

			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", null)));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "SYSTEM TABLE")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "SYSTEM table")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "LOCAL TEMPORARY")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "local TEMPORARY")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "GLOBAL TEMPORARY")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "global temporary")));

			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "unknown")));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs
					.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Arrays.asList("view")));
			dbTableTypeSpecs
					.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(),
							Arrays.asList("table", "null", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "TABLE")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "table")));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", null)));
			assertTrue(resolver.isDataTable(connection, new SimpleTable("test", "unknown")));

			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "A-TABLE-B")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "view")));
			assertFalse(resolver.isDataTable(connection, new SimpleTable("test", "VIEW")));
		}
	}

	@Test
	public void isDataTablesTest_Array() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			boolean[] re = resolver.isDataTables(connection, new SimpleTable[] { new SimpleTable("test", "table"),
					new SimpleTable("test", "view"), new SimpleTable("test", null),
					new SimpleTable("test", "SYSTEM TABLE"), new SimpleTable("test", "unknown") });

			assertTrue(re[0]);
			assertTrue(re[1]);
			assertFalse(re[2]);
			assertFalse(re[3]);
			assertFalse(re[4]);
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Arrays.asList("view")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(),
					Arrays.asList("table", "NULL", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			boolean[] re = resolver.isDataTables(connection,
					new SimpleTable[] { new SimpleTable("test", "TABLE"), new SimpleTable("test", "table"),
							new SimpleTable("test", "A-TABLE-B"), new SimpleTable("test", "unknown"),
							new SimpleTable("test", null), new SimpleTable("test", "view"),
							new SimpleTable("test", "VIEW") });

			assertTrue(re[0]);
			assertTrue(re[1]);
			assertFalse(re[2]);
			assertTrue(re[3]);
			assertTrue(re[4]);
			assertFalse(re[5]);
			assertFalse(re[6]);
		}
	}

	@Test
	public void isDataTablesTest_List() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			List<Boolean> re = resolver.isDataTables(connection,
					Arrays.asList(new SimpleTable("test", "table"), new SimpleTable("test", "view"),
							new SimpleTable("test", null), new SimpleTable("test", "SYSTEM TABLE"),
							new SimpleTable("test", "unknown")));

			assertTrue(re.get(0));
			assertTrue(re.get(1));
			assertFalse(re.get(2));
			assertFalse(re.get(3));
			assertFalse(re.get(4));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Arrays.asList("view")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(),
					Arrays.asList("table", "NULL", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			List<Boolean> re = resolver.isDataTables(connection,
					Arrays.asList( new SimpleTable("test", "TABLE"), new SimpleTable("test", "table"),
							new SimpleTable("test", "A-TABLE-B"), new SimpleTable("test", "unknown"),
							new SimpleTable("test", null), new SimpleTable("test", "view"),
							new SimpleTable("test", "VIEW") ));

			assertTrue(re.get(0));
			assertTrue(re.get(1));
			assertFalse(re.get(2));
			assertTrue(re.get(3));
			assertTrue(re.get(4));
			assertFalse(re.get(5));
			assertFalse(re.get(6));
		}
	}

	@Test
	public void isEntityTableTest() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", "table")));
			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", "TABLE")));

			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "view")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "VIEW")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "alias")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "AliAs")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "synonym")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "SYnonyM")));

			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", null)));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "SYSTEM TABLE")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "SYSTEM table")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "LOCAL TEMPORARY")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "local TEMPORARY")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "GLOBAL TEMPORARY")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "global temporary")));

			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "unknown")));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("view")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("table", "NULL", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", "TABLE")));
			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", "table")));
			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", null)));
			assertTrue(resolver.isEntityTable(connection, new SimpleTable("test", "unknown")));

			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "A-TABLE-B")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "a-table-b")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "view")));
			assertFalse(resolver.isEntityTable(connection, new SimpleTable("test", "VIEW")));
		}
	}

	@Test
	public void isEntityTablesTest_Array() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			boolean[] re = resolver.isEntityTables(connection,
					new SimpleTable[] { new SimpleTable("test", "table"), new SimpleTable("test", "VIEW") });

			assertTrue(re[0]);
			assertFalse(re[1]);
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("view")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("table", "NULL", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			boolean[] re = resolver.isEntityTables(connection,
					new SimpleTable[] { new SimpleTable("test", "TABLE"), new SimpleTable("test", null),
							new SimpleTable("test", "view") });

			assertTrue(re[0]);
			assertTrue(re[1]);
			assertFalse(re[2]);
		}
	}

	@Test
	public void isEntityTablesTest_List() throws Exception
	{
		// 无DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();

			List<Boolean> re = resolver.isEntityTables(connection,
					Arrays.asList(new SimpleTable("test", "table"), new SimpleTable("test", "VIEW")));

			assertTrue(re.get(0));
			assertFalse(re.get(1));
		}

		// 有DbTableTypeSpec
		{
			DefaultTableTypeResolver resolver = new DefaultTableTypeResolver();
			List<DbTableTypeSpec> dbTableTypeSpecs = new ArrayList<>();
			dbTableTypeSpecs.add(new DbTableTypeSpec("*oracle*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("view")));
			dbTableTypeSpecs.add(new DbTableTypeSpec("*mysql*", Collections.emptyList(), Collections.emptyList(),
					Arrays.asList("table", "NULL", "unknown")));
			resolver.setDbTableTypeSpecs(dbTableTypeSpecs);

			List<Boolean> re = resolver.isEntityTables(connection, Arrays.asList(new SimpleTable("test", "TABLE"),
					new SimpleTable("test", null), new SimpleTable("test", "view")));

			assertTrue(re.get(0));
			assertTrue(re.get(1));
			assertFalse(re.get(2));
		}
	}
}
