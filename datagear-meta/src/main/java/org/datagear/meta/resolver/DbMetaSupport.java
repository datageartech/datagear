/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.meta.resolver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.datagear.util.JDBCCompatiblity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库元信息支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DbMetaSupport
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DbMetaSupport.class);

	public DbMetaSupport()
	{
		super();
	}

	/**
	 * 获取{@linkplain DatabaseMetaData}。
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	public DatabaseMetaData getDatabaseMetaData(Connection cn) throws DBMetaResolverException
	{
		try
		{
			return cn.getMetaData();
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
	}

	/**
	 * 获取当前连接的catalog。
	 * 
	 * @param cn
	 * @return
	 * @throws SQLException
	 */
	public String getCatalog(Connection cn) throws DBMetaResolverException
	{
		try
		{
			return cn.getCatalog();
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
	}

	/**
	 * 获取当前连接的schema。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @return
	 * @throws DBMetaResolverException
	 */
	public String getSchema(Connection cn, DatabaseMetaData databaseMetaData) throws DBMetaResolverException
	{
		String schema;

		try
		{
			@JDBCCompatiblity("JDBC4.1（JDK1.7）才有Connection.getSchema()接口，为了兼容JDBC4.0（JDK1.6），"
					+ "所以这里捕获Throwable，避免出现底层java.lang.Error")
			String mySchema = cn.getSchema();
			schema = mySchema;
		}
		catch (Throwable e)
		{
			LOGGER.warn("current schema will be set to null for error:", e);

			@JDBCCompatiblity("在JDBC4.0（JDK1.6）中需要将其设置为null，才符合DatabaseMetaData.getTables(...)等接口的参数要求")
			String mySchema = null;
			schema = mySchema;
		}

		return schema;
	}

	/**
	 * 获取标识符引用符。
	 * 
	 * @param cn
	 * @return
	 */
	public String getIdentifierQuote(Connection cn)
	{
		String iq = null;

		try
		{
			iq = cn.getMetaData().getIdentifierQuoteString();
		}
		catch (SQLException e)
		{

		}

		if (iq == null || iq.isEmpty())
		{
			@JDBCCompatiblity("出现异常、，或者不规范的JDBC驱动返回空字符串时，使用JDBC规范规定的空格字符串")
			String iqt = " ";

			iq = iqt;
		}

		return iq;
	}
}
