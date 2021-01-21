/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection;

import java.io.Serializable;
import java.sql.Connection;

import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;

/**
 * 数据库对象标识。
 * <p>
 * 它使用数据库名称、数据库版本标识数据库。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseIdentity implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String productName;

	private final String productVersion;

	private final int majorVersion;

	private final int minorVersion;

	public DatabaseIdentity(String productName, String productVersion, int majorVersion, int minorVersion)
	{
		super();
		this.productName = productName;
		this.productVersion = productVersion;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	public String getProductName()
	{
		return productName;
	}

	public String getProductVersion()
	{
		return productVersion;
	}

	public int getMajorVersion()
	{
		return majorVersion;
	}

	public int getMinorVersion()
	{
		return minorVersion;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + majorVersion;
		result = prime * result + minorVersion;
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((productVersion == null) ? 0 : productVersion.hashCode());
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
		DatabaseIdentity other = (DatabaseIdentity) obj;
		if (majorVersion != other.majorVersion)
			return false;
		if (minorVersion != other.minorVersion)
			return false;
		if (productName == null)
		{
			if (other.productName != null)
				return false;
		}
		else if (!productName.equals(other.productName))
			return false;
		if (productVersion == null)
		{
			if (other.productVersion != null)
				return false;
		}
		else if (!productVersion.equals(other.productVersion))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [productName=" + productName + ", productVersion=" + productVersion
				+ ", majorVersion="
				+ majorVersion + ", minorVersion=" + minorVersion + "]";
	}

	public static DatabaseIdentity valueOf(String productName, String productVersion, int majorVersion,
			int minorVersion)
	{
		return new DatabaseIdentity(productName, productVersion, majorVersion, minorVersion);
	}

	/**
	 * 获取指定{@linkplain Connection}的{@linkplain DatabaseIdentity}。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示无法确定
	 */
	public static DatabaseIdentity valueOf(Connection cn)
	{
		String productName = JdbcUtil.getDatabaseProductNameIfSupports(cn);
		String productVersion = JdbcUtil.getDatabaseProductVersionIfSupports(cn);
		Integer majorVersion = JdbcUtil.getDatabaseMajorVersionIfSupports(cn);
		Integer minorVersion = JdbcUtil.getDatabaseMinorVersionIfSupports(cn);

		if (StringUtil.isEmpty(productName))
			return null;

		// 产品版本和主版本号必有其一
		if (StringUtil.isEmpty(productName) && majorVersion == null)
			return null;
		
		if(productVersion == null)
			productVersion = "";
		
		if(majorVersion == null)
			majorVersion = -1;
		
		if(minorVersion == null)
			minorVersion = -1;

		return new DatabaseIdentity(productName, productVersion, majorVersion, minorVersion);
	}
}
