/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;

/**
 * 不支持的数据集结果数据异常。
 * <p>
 * 当数据集结果数据对象不符合{@linkplain DataSetResult#getData()}所要求的类型时，将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedResultDataException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedResultDataException()
	{
		super();
	}

	public UnsupportedResultDataException(String message)
	{
		super(message);
	}

	public UnsupportedResultDataException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedResultDataException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
