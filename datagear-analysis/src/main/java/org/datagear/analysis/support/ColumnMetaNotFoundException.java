/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.ColumnMeta;
import org.datagear.analysis.DataSetException;

/**
 * 指定名称的{@linkplain ColumnMeta}未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnMetaNotFoundException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String name;

	public ColumnMetaNotFoundException(String name)
	{
		super("Column [" + name + "] not found");
		this.name = name;
	}

	public ColumnMetaNotFoundException(String name, String message)
	{
		super(message);
		this.name = name;
	}

	public ColumnMetaNotFoundException(String name, Throwable cause)
	{
		super(cause);
		this.name = name;
	}

	public ColumnMetaNotFoundException(String name, String message, Throwable cause)
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
