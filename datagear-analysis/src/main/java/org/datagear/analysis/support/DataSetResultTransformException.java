/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * {@linkplain DataSetResultTransformer}转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResultTransformException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public DataSetResultTransformException()
	{
		super();
	}

	public DataSetResultTransformException(String message)
	{
		super(message);
	}

	public DataSetResultTransformException(Throwable cause)
	{
		super(cause);
	}

	public DataSetResultTransformException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
