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
