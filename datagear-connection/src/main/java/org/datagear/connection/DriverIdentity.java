/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.io.Serializable;
import java.sql.Driver;

/**
 * JDBC驱动程序对象标识。
 * <p>
 * 它使用驱动类名、版本号标识JDBC驱动程序。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DriverIdentity implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String driverClassName;

	private final int majorVersion;

	private final int minorVersion;

	public DriverIdentity(String driverClassName, int majorVersion, int minorVersion)
	{
		super();
		this.driverClassName = driverClassName;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	public String getDriverClassName()
	{
		return driverClassName;
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
		result = prime * result + ((driverClassName == null) ? 0 : driverClassName.hashCode());
		result = prime * result + majorVersion;
		result = prime * result + minorVersion;
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
		DriverIdentity other = (DriverIdentity) obj;
		if (driverClassName == null)
		{
			if (other.driverClassName != null)
				return false;
		}
		else if (!driverClassName.equals(other.driverClassName))
			return false;
		if (majorVersion != other.majorVersion)
			return false;
		if (minorVersion != other.minorVersion)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [driverClassName=" + driverClassName + ", majorVersion=" + majorVersion
				+ ", minorVersion=" + minorVersion + "]";
	}

	public static DriverIdentity valueOf(Driver driver)
	{
		return new DriverIdentity(driver.getClass().getName(), driver.getMajorVersion(), driver.getMinorVersion());
	}
}
