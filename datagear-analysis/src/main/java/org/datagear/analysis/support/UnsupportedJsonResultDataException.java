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

/**
 * 不支持的JSON数据集结果数据异常。
 * <p>
 * JSON数据集结果数据必须为对象、对象数组。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedJsonResultDataException extends UnsupportedResultDataException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedJsonResultDataException()
	{
		super();
	}

	public UnsupportedJsonResultDataException(String message)
	{
		super(message);
	}

	public UnsupportedJsonResultDataException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedJsonResultDataException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
