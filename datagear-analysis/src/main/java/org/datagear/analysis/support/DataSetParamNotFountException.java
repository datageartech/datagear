/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetParam;

/**
 * 指定名称的{@linkplain DataSetParam}不存在异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamNotFountException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String name;

	public DataSetParamNotFountException(String name)
	{
		super();
		this.name = name;
	}

	public DataSetParamNotFountException(String name, String message)
	{
		super(message);
		this.name = name;
	}

	public DataSetParamNotFountException(String name, Throwable cause)
	{
		super(cause);
		this.name = name;
	}

	public DataSetParamNotFountException(String name, String message, Throwable cause)
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
