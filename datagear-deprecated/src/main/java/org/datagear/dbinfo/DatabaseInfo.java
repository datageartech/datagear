/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * 数据库信息。
 * <p>
 * 类结构参考{@linkplain DatabaseMetaData}和{@linkplain Connection}相关方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseInfo implements Serializable
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

	public DatabaseInfo()
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
