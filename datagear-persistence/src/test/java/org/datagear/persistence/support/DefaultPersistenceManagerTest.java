/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.datagear.meta.Table;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Row;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.test.DBTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain DefaultPersistenceManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPersistenceManagerTest extends DBTestSupport
{
	private GenericDBMetaResolver genericDBMetaResolver;
	private DefaultPersistenceManager defaultPersistenceManager;

	private Connection connection;

	public DefaultPersistenceManagerTest()
	{
		super();

		this.genericDBMetaResolver = new GenericDBMetaResolver();
		DialectSource dialectSource = new DefaultDialectSource(genericDBMetaResolver);
		this.defaultPersistenceManager = new DefaultPersistenceManager(dialectSource);
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
	public void insertTest() throws Exception
	{
		Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");

		int id = 999999999;
		String name = "NAME-FOR-TEST";
		String INTRODUCTION = "INTRODUCTION-for-test";

		Row row = new Row();
		row.put("ID", id);
		row.put("NAME", name);
		row.put("HEAD_IMG", "hex:0x09");
		row.put("INTRODUCTION", INTRODUCTION);

		try
		{
			this.defaultPersistenceManager.delete(connection, table, row);

			this.defaultPersistenceManager.insert(connection, null, table, row, new ConversionSqlParamValueMapper());

			Row getRow = this.defaultPersistenceManager.get(connection, table, row);

			assertEquals(id, ((Number) getRow.get("ID")).intValue());
			assertEquals(name, getRow.get("NAME"));
			assertArrayEquals(new byte[] { 0x09 }, columnValueToBytes(getRow.get("HEAD_IMG")));
			assertEquals(INTRODUCTION, columnValueToString(getRow.get("INTRODUCTION")));
		}
		finally
		{
			this.defaultPersistenceManager.delete(connection, table, row);
		}
	}

	@Test
	public void pagingQueryTest()
	{
		Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");

		PagingData<Row> pagingData = this.defaultPersistenceManager.pagingQuery(connection, table,
				new PagingQuery(1, 1));
		List<Row> rows = pagingData.getItems();

		assertTrue(rows.size() <= 1);
	}

	protected byte[] columnValueToBytes(Object o) throws Exception
	{
		if (o instanceof byte[])
			return (byte[]) o;
		else if (o instanceof InputStream)
			return IOUtil.getBytes((InputStream) o);
		else
			throw new UnsupportedOperationException();
	}

	protected String columnValueToString(Object o) throws Exception
	{
		if (o instanceof String)
			return (String) o;
		else if (o instanceof Reader)
			return IOUtil.readString((Reader) o, true);
		else if (o instanceof InputStream)
		{
			Reader reader = IOUtil.getReader((InputStream) o, null);
			return IOUtil.readString(reader, true);
		}
		else
			throw new UnsupportedOperationException();
	}
}
