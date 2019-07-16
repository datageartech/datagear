/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Serializable;

import org.datagear.dataexchange.ExceptionResolve;

/**
 * SQL导入设置项。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImportOption implements Serializable
{
	private static final long serialVersionUID = 1L;

	private ExceptionResolve exceptionResolve;

	public SqlDataImportOption()
	{
		super();
	}

	public SqlDataImportOption(ExceptionResolve exceptionResolve)
	{
		super();
		this.exceptionResolve = exceptionResolve;
	}

	public ExceptionResolve getExceptionResolve()
	{
		return exceptionResolve;
	}

	public void setExceptionResolve(ExceptionResolve exceptionResolve)
	{
		this.exceptionResolve = exceptionResolve;
	}
}
