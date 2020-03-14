/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.ArrayMatching.arrayContaining;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.resolver.DevotedDBMetaResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.meta.resolver.WildcardDevotedDBMetaResolver;
import org.datagear.meta.resolver.support.MySqlDevotedDBMetaResolver;
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

		List<DevotedDBMetaResolver> devotedDBMetaResolvers = new ArrayList<DevotedDBMetaResolver>();
		devotedDBMetaResolvers.add(new MySqlDevotedDBMetaResolver());
		devotedDBMetaResolvers.add(new WildcardDevotedDBMetaResolver());

		this.genericDBMetaResolver = new GenericDBMetaResolver(devotedDBMetaResolvers);
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

		assertThat(simpleTables, hasItem(hasProperty("name", equalTo("T_ACCOUNT"))));
		assertThat(simpleTables, hasItem(hasProperty("name", equalTo("T_ADDRESS"))));
	}

	@Test
	public void getTableTest() throws Exception
	{
		{
			Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");
			assertThat(table, hasProperty("name", equalTo("T_ACCOUNT")));
			assertThat(table.getColumns(), hasItemInArray(hasProperty("name", equalTo("ID"))));
			assertThat(table.getPrimaryKey(), hasProperty("columnNames", hasItemInArray(equalTo("ID"))));
		}

		{
			Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ADDRESS");
			assertThat(table, hasProperty("name", equalTo("T_ADDRESS")));
			assertThat(table.getColumns(), hasItemInArray(hasProperty("name", equalTo("ACCOUNT_ID"))));
			assertThat(table.getUniqueKeys(),
					hasItemInArray(hasProperty("columnNames", arrayContaining("ACCOUNT_ID"))));
		}
	}
}
