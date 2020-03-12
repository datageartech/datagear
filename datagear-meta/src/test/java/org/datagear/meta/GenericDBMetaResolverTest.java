/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
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

	public GenericDBMetaResolverTest()
	{
		super();

		List<DevotedDBMetaResolver> devotedDBMetaResolvers = new ArrayList<DevotedDBMetaResolver>();
		devotedDBMetaResolvers.add(new MySqlDevotedDBMetaResolver());
		devotedDBMetaResolvers.add(new WildcardDevotedDBMetaResolver());

		this.genericDBMetaResolver = new GenericDBMetaResolver(devotedDBMetaResolvers);
	}

	@Test
	public void getSimpleTablesTest() throws Exception
	{
		Connection cn = null;

		List<SimpleTable> simpleTables = null;

		try
		{
			cn = getConnection();

			simpleTables = this.genericDBMetaResolver.getSimpleTables(cn);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		assertThat(simpleTables, hasItem(hasProperty("name", equalTo("T_ACCOUNT"))));
		assertThat(simpleTables, hasItem(hasProperty("name", equalTo("T_ADDRESS"))));
	}

	@Test
	public void getTableTest() throws Exception
	{
		Connection cn = null;

		Table table0 = null;
		Table table1 = null;

		try
		{
			cn = getConnection();

			table0 = this.genericDBMetaResolver.getTable(cn, "T_ACCOUNT");
			table1 = this.genericDBMetaResolver.getTable(cn, "T_ADDRESS");
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		assertThat(table0, hasProperty("name", equalTo("T_ACCOUNT")));
		assertThat(table0, hasProperty("columns", hasItemInArray(hasProperty("name", equalTo("ID")))));
		assertThat(table0, hasProperty("primaryKey", hasProperty("columnNames", hasItemInArray(equalTo("ID")))));

		assertThat(table1, hasProperty("name", equalTo("T_ADDRESS")));
		assertThat(table1, hasProperty("columns", hasItemInArray(hasProperty("name", equalTo("ACCOUNT_ID")))));
	}
}
