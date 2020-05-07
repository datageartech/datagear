/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataType;

/**
 * 数据值转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataValueConvertionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private Object source;

	private DataType type;

	public DataValueConvertionException(Object source, DataType type)
	{
		super();
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, DataType type, String message)
	{
		super(message);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, DataType type, Throwable cause)
	{
		super(cause);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, DataType type, String message, Throwable cause)
	{
		super(message, cause);
		this.type = type;
		this.source = source;
	}

	public Object getSource()
	{
		return source;
	}

	protected void setSource(Object source)
	{
		this.source = source;
	}

	public DataType getType()
	{
		return type;
	}

	protected void setType(DataType type)
	{
		this.type = type;
	}

}
