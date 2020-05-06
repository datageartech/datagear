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
 * 数据集参数值转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueConverterException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private DataSetParam param;

	private Object source;

	public DataSetParamValueConverterException(DataSetParam param, Object source)
	{
		super();
		this.param = param;
		this.source = source;
	}

	public DataSetParamValueConverterException(DataSetParam param, Object source, String message)
	{
		super(message);
		this.param = param;
		this.source = source;
	}

	public DataSetParamValueConverterException(DataSetParam param, Object source, Throwable cause)
	{
		super(cause);
		this.param = param;
		this.source = source;
	}

	public DataSetParamValueConverterException(DataSetParam param, Object source, String message, Throwable cause)
	{
		super(message, cause);
		this.param = param;
		this.source = source;
	}

	public DataSetParam getParam()
	{
		return param;
	}

	protected void setParam(DataSetParam param)
	{
		this.param = param;
	}

	public Object getSource()
	{
		return source;
	}

	protected void setSource(Object source)
	{
		this.source = source;
	}
}
