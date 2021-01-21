/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * {@linkplain TemplateResolver}异常。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateResolverException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public TemplateResolverException()
	{
		super();
	}

	public TemplateResolverException(String message)
	{
		super(message);
	}

	public TemplateResolverException(Throwable cause)
	{
		super(cause);
	}

	public TemplateResolverException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
