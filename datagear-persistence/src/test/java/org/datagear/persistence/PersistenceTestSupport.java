/*
 * Copyright 2018-present datagear.tech
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
