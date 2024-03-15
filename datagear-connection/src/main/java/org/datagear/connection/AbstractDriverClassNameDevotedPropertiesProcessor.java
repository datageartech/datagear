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

package org.datagear.connection;

import java.sql.Driver;
import java.util.Properties;

/**
 * 抽象驱动类名{@linkplain DevotedPropertiesProcessor}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDriverClassNameDevotedPropertiesProcessor implements DevotedPropertiesProcessor
{
	private String driverClassName;

	private boolean partial;

	public AbstractDriverClassNameDevotedPropertiesProcessor()
	{
		super();
	}

	public AbstractDriverClassNameDevotedPropertiesProcessor(String driverClassName, boolean partial)
	{
		super();
		this.driverClassName = driverClassName;
		this.partial = partial;
	}

	public String getDriverClassName()
	{
		return driverClassName;
	}

	protected void setDriverClassName(String driverClassName)
	{
		this.driverClassName = driverClassName;
	}

	public boolean isPartial()
	{
		return partial;
	}

	protected void setPartial(boolean partial)
	{
		this.partial = partial;
	}

	@Override
	public boolean supports(Driver driver, String url, Properties properties)
	{
		String driverClassName = driver.getClass().getName();

		if (partial)
			return (driverClassName.toLowerCase().indexOf(this.driverClassName.toLowerCase()) >= 0);
		else
			return driverClassName.equalsIgnoreCase(this.driverClassName);
	}

	/**
	 * 指定名称的连接属性是否已设置。
	 * 
	 * @param url
	 * @param properties
	 * @param propertyName
	 * @return
	 */
	protected boolean isPropertyPresent(String url, Properties properties, String propertyName)
	{
		return (url.indexOf(propertyName + "=") >= 0 || properties.getProperty(propertyName) != null);
	}
}
