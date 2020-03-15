/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.datagear.meta.Table;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PstValueConverter;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
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

	private DialectSource dialectSource;

	private DefaultPersistenceManager defaultPersistenceManager;

	private Connection connection;
	private Dialect dialect;

	public DefaultPersistenceManagerTest()
	{
		super();

		this.genericDBMetaResolver = new GenericDBMetaResolver();
		this.dialectSource = new DefaultDialectSource(this.genericDBMetaResolver);
		this.defaultPersistenceManager = new DefaultPersistenceManager();
	}

	@Before
	public void init() throws Exception
	{
		this.connection = getConnection();
		this.dialect = this.dialectSource.getDialect(this.connection);
	}

	@After
	public void destroy()
	{
		JdbcUtil.closeConnection(this.connection);
	}

	@Test
	public void insertTest()
	{
		Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");
		PstValueConverter converter = new SimplePstValueConverter();
		RowMapper mapper = new SimpleRowMapper();
		
		int id = 999999999;
		String name = "NAME-FOR-TEST";
		
		Row row = new Row();
		row.put("ID", id);
		row.put("NAME", name);
		
		try
		{
			this.defaultPersistenceManager.insert(connection, dialect, table, row, converter);

			Row getRow = this.defaultPersistenceManager.get(connection, dialect, table, row, converter, mapper);

			assertEquals(id, ((Number) getRow.get("ID")).intValue());
			assertEquals(name, getRow.get("NAME"));
		}
		finally
		{
			this.defaultPersistenceManager.delete(connection, dialect, table, row, converter);
		}
	}
}
