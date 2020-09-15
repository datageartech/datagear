/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

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

	private String type;

	public DataValueConvertionException(Object source, String type)
	{
		super("Convert from [" + source + "] to [" + type + "] is not supported");
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, String message)
	{
		super(message);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, Throwable cause)
	{
		super(cause);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, String message, Throwable cause)
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

	public String getType()
	{
		return type;
	}

	protected void setType(String type)
	{
		this.type = type;
	}

}
