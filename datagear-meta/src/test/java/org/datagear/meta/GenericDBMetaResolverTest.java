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

import java.sql.Connection;
import java.util.List;

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
	public void getSimpleTablesTest() throws Exception
	{
		List<SimpleTable> simpleTables = this.genericDBMetaResolver.getSimpleTables(this.connection);

		assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ACCOUNT"))));
		assertThat(simpleTables, hasItem(hasProperty("name", equalToIgnoringCase("T_ADDRESS"))));
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
}
