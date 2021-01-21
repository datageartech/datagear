/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
