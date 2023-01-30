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

package org.datagear.meta;

import java.io.Serializable;

/**
 * 数据库信息。
 * 
 * @author datagear@163.com
 *
 */
public class Database implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String url;

	private String user;

	private String productName;

	private String productVersion;

	private String catalog;

	private String schema;

	private String driverName;

	private String driverVersion;

	public Database()
	{
		super();
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getProductVersion()
	{
		return productVersion;
	}

	public void setProductVersion(String productVersion)
	{
		this.productVersion = productVersion;
	}

	public String getCatalog()
	{
		return catalog;
	}

	public void setCatalog(String catalog)
	{
		this.catalog = catalog;
	}

	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	public String getDriverName()
	{
		return driverName;
	}

	public void setDriverName(String driverName)
	{
		this.driverName = driverName;
	}

	public String getDriverVersion()
	{
		return driverVersion;
	}

	public void setDriverVersion(String driverVersion)
	{
		this.driverVersion = driverVersion;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [url=" + url + ", user=" + user + ", productName=" + productName
				+ ", productVersion=" + productVersion + ", catalog=" + catalog + ", schema=" + schema + ", driverName="
				+ driverName + ", driverVersion=" + driverVersion + "]";
	}
}
