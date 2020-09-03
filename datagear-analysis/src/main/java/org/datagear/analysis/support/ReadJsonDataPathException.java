/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

/**
 * 读取指定JSON路径的数据异常。
 * 
 * @author datagear@163.com
 *
 */
public class ReadJsonDataPathException extends DataSetSourceParseException
{
	private static final long serialVersionUID = 1L;

	private String dataPath;

	public ReadJsonDataPathException(String dataPath)
	{
		super();
		this.dataPath = dataPath;
	}

	public ReadJsonDataPathException(String dataPath, String message)
	{
		super(message);
		this.dataPath = dataPath;
	}

	public ReadJsonDataPathException(String dataPath, Throwable cause)
	{
		super(cause);
		this.dataPath = dataPath;
	}

	public ReadJsonDataPathException(String dataPath, String message, Throwable cause)
	{
		super(message, cause);
		this.dataPath = dataPath;
	}

	public String getDataPath()
	{
		return dataPath;
	}

	protected void setDataPath(String dataPath)
	{
		this.dataPath = dataPath;
	}
}
