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

package org.datagear.connection;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 数据库连接标识。
 * <p>
 * 它使用连接URL、连接参数标识数据库连接。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConnectionIdentity implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String url;

	private final Map<?, ?> properties;

	public ConnectionIdentity(String url, Map<?, ?> properties)
	{
		super();
		this.url = url;
		this.properties = Collections.unmodifiableMap(properties);
	}

	public String getUrl()
	{
		return url;
	}

	public Map<?, ?> getProperties()
	{
		return properties;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionIdentity other = (ConnectionIdentity) obj;
		if (properties == null)
		{
			if (other.properties != null)
				return false;
		}
		else if (!properties.equals(other.properties))
			return false;
		if (url == null)
		{
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [url=" + url + ", properties=" + properties + "]";
	}

	public static ConnectionIdentity valueOf(String url, Map<?, ?> properties)
	{
		return new ConnectionIdentity(url, properties);
	}

	public static ConnectionIdentity valueOf(ConnectionOption connectionOption)
	{
		return new ConnectionIdentity(connectionOption.getUrl(), connectionOption.getProperties());
	}
}
