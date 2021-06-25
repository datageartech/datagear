/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * 数据集参数值必填异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueRequiredException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public DataSetParamValueRequiredException()
	{
		super();
	}

	public DataSetParamValueRequiredException(String message)
	{
		super(message);
	}

	public DataSetParamValueRequiredException(Throwable cause)
	{
		super(cause);
	}

	public DataSetParamValueRequiredException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
