/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 数据库模式属性。
 * <p>
 * 数据库JDBC连接属性。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SchemaProperty implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**属性名*/
	private String name;
	
	/**属性值*/
	private String value;

	public SchemaProperty()
	{
		super();
	}

	public SchemaProperty(String name, String value)
	{
		super();
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
