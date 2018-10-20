/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

/**
 * 未找到驱动异常。
 * 
 * @author datagear@163.com
 *
 */
public class DriverNotFoundException extends PathDriverFactoryException
{
	private static final long serialVersionUID = 1L;

	private String driverPath;

	private String driverClassName;

	public DriverNotFoundException(String driverPath, String driverClassName)
	{
		this(driverPath, driverClassName, null);
	}

	public DriverNotFoundException(String driverPath, String driverClassName, Throwable cause)
	{
		super("Driver [" + driverClassName + "] not found int path [" + driverPath + "]", cause);

		this.driverPath = driverPath;
		this.driverClassName = driverClassName;
	}

	public String getDriverPath()
	{
		return driverPath;
	}

	public String getDriverClassName()
	{
		return driverClassName;
	}
}
