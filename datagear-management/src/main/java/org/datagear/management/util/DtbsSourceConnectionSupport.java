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
import java.util.List;
import java.util.Properties;

import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverEntity;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.DtbsSourceProperty;
import org.datagear.util.StringUtil;

/**
 * {@linkplain DtbsSource}数据库连接支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceConnectionSupport
{
	public DtbsSourceConnectionSupport()
	{
	}

	/**
	 * 获取指定{@linkplain DtbsSource}的{@linkplain Connection}。
	 * 
	 * @param connectionSource
	 * @param schema
	 * @return
	 * @throws ConnectionSourceException
	 */
	public Connection getSchemaConnection(ConnectionSource connectionSource, DtbsSource schema)
			throws ConnectionSourceException
	{
		Connection cn = null;
		
		Properties properties = null;
		
		if(schema.hasProperty())
		{
			properties = new Properties();
			
			List<DtbsSourceProperty> schemaProperties = schema.getProperties();
			for(DtbsSourceProperty sp : schemaProperties)
			{
				String name = sp.getName();
				String value = sp.getValue();
				
				if(!StringUtil.isEmpty(name))
					properties.put(name, (value == null ? "" : value));
			}
		}
		
		ConnectionOption connectionOption = ConnectionOption.valueOf(schema.getUrl(), schema.getUser(),
				schema.getPassword(), properties);

		if (schema.hasDriverEntity())
		{
			DriverEntity driverEntity = schema.getDriverEntity();

			cn = connectionSource.getConnection(driverEntity, connectionOption);
		}
		else
		{
			cn = connectionSource.getConnection(connectionOption);
		}

		return cn;
	}
}
