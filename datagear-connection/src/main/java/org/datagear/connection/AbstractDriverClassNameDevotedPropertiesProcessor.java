/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
