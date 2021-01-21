/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;

/**
 * 指定名称的{@linkplain DataSetProperty}未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyNotFoundException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String name;

	public DataSetPropertyNotFoundException(String name)
	{
		super("Property [" + name + "] not found");
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, String message)
	{
		super(message);
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, Throwable cause)
	{
		super(cause);
		this.name = name;
	}

	public DataSetPropertyNotFoundException(String name, String message, Throwable cause)
	{
		super(message, cause);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	protected void setName(String name)
	{
		this.name = name;
	}
}
