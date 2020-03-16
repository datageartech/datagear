/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.model.Model;

/**
 * 不支持解析列模型异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedColumnModelResolvingException extends DatabaseModelResolverException
{
	private static final long serialVersionUID = 1L;

	private ColumnInfo columnInfo;

	public UnsupportedColumnModelResolvingException()
	{
		super();
	}

	public UnsupportedColumnModelResolvingException(ColumnInfo columnInfo)
	{
		super("Resolving [" + columnInfo + "] 's [" + Model.class.getSimpleName() + "] is not supported");
		this.columnInfo = columnInfo;
	}

	public ColumnInfo getColumnInfo()
	{
		return columnInfo;
	}
}
