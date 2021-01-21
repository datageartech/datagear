/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence;

import java.sql.Connection;
import java.sql.Types;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.meta.TableType;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.support.DefaultDialectSource;
import org.datagear.util.JdbcUtil;
import org.datagear.util.test.DBTestSupport;
import org.junit.After;
import org.junit.Before;

public class PersistenceTestSupport extends DBTestSupport
{
	protected GenericDBMetaResolver genericDBMetaResolver;

	protected DialectSource dialectSource;

	protected Connection connection;

	public PersistenceTestSupport()
	{
		super();
		this.genericDBMetaResolver = new GenericDBMetaResolver();
		this.dialectSource = new DefaultDialectSource(genericDBMetaResolver);
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

	public static Column MOCK_COLUMN_ID = new Column("ID", Types.BIGINT);
	public static Column MOCK_COLUMN_NAME = new Column("NAME", Types.VARCHAR);
	public static Column MOCK_COLUMN_DESC = new Column("DESC", Types.VARCHAR);
	public static Table MOCK_TABLE = new Table("MOCK_TABLE", TableType.VIEW,
			new Column[] { MOCK_COLUMN_ID, MOCK_COLUMN_NAME, MOCK_COLUMN_DESC });
}
