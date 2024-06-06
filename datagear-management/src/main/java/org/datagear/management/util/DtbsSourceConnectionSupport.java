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

package org.datagear.management.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.datasource.DataSourceException;
import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverEntity;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.DtbsSourceProperty;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;

/**
 * {@linkplain DtbsSource}数据库连接支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceConnectionSupport
{
	protected static final String INTERNAL_SCHEMA_PROPERTY_NAME = Global.NAME_SHORT_UCUS + "INTERNAL_SCHEMA_NAME";

	public DtbsSourceConnectionSupport()
	{
	}

	/**
	 * 获取指定{@linkplain DtbsSource}的{@linkplain Connection}。
	 * 
	 * @param connectionSource
	 * @param dtbsSource
	 * @return
	 * @throws ConnectionSourceException
	 */
	public Connection getDtbsSourceConnection(ConnectionSource connectionSource, DtbsSource dtbsSource)
			throws ConnectionSourceException
	{
		Connection cn = null;
		
		Properties properties = new Properties();
		
		if(dtbsSource.hasProperty())
		{
			List<DtbsSourceProperty> dtbsSourceProperties = dtbsSource.getProperties();
			for(DtbsSourceProperty sp : dtbsSourceProperties)
			{
				String name = sp.getName();
				String value = sp.getValue();
				
				if(!StringUtil.isEmpty(name))
					properties.put(name, (value == null ? "" : value));
			}
		}
		
		String schemaName = dtbsSource.getSchemaName();
		boolean emptySchemaName = StringUtil.isEmpty(schemaName);

		// 必须添加此连接属性，不然会获取到其他数据库模式的连接
		if (!emptySchemaName)
		{
			properties.put(INTERNAL_SCHEMA_PROPERTY_NAME, schemaName);
		}

		ConnectionOption connectionOption = ConnectionOption.valueOf(dtbsSource.getUrl(), dtbsSource.getUser(),
				dtbsSource.getPassword(), properties);

		if (dtbsSource.hasDriverEntity())
		{
			DriverEntity driverEntity = dtbsSource.getDriverEntity();
			cn = connectionSource.getConnection(driverEntity, connectionOption);
		}
		else
		{
			cn = connectionSource.getConnection(connectionOption);
		}

		if (!emptySchemaName)
		{
			try
			{
				cn.setSchema(dtbsSource.getSchemaName());
			}
			catch (SQLException e)
			{
				throw new DataSourceException(e);
			}
		}

		return cn;
	}
}
