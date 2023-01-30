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

package org.datagear.management.domain;

import java.io.Serializable;
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
public class SchemaConnectionFactory extends SchemaConnectionSupport implements ConnectionFactory, Serializable
{
	private static final long serialVersionUID = 1L;

	private transient ConnectionSource connectionSource;

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
