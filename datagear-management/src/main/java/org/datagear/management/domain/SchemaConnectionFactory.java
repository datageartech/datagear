/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.sql.Connection;

import org.datagear.connection.ConnectionSource;
import org.datagear.management.util.SchemaConnectionSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ConnectionFactory;

/**
 * 封装{@linkplain Schema}的{@linkplain ConnectionFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaConnectionFactory extends SchemaConnectionSupport implements ConnectionFactory
{
	private ConnectionSource connectionSource;

	private Schema schema;

	public SchemaConnectionFactory()
	{
		super();
	}

	public SchemaConnectionFactory(ConnectionSource connectionSource, Schema schema)
	{
		super();
		this.connectionSource = connectionSource;
		this.schema = schema;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}

	@Override
	public Connection get() throws Exception
	{
		return super.getSchemaConnection(this.connectionSource, this.schema);
	}

	@Override
	public void release(Connection resource) throws Exception
	{
		JdbcUtil.closeConnection(resource);
	}
}
