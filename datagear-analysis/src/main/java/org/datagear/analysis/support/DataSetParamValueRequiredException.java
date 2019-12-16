/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * 指定参数值必填异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueRequiredException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String name;

	public DataSetParamValueRequiredException(String name)
	{
		super();
		this.name = name;
	}

	public DataSetParamValueRequiredException(String name, String message)
	{
		super(message);
		this.name = name;
	}

	public DataSetParamValueRequiredException(String name, Throwable cause)
	{
		super(cause);
		this.name = name;
	}

	public DataSetParamValueRequiredException(String name, String message, Throwable cause)
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
