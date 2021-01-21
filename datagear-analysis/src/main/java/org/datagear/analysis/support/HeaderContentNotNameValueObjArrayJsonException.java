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
 * {@linkplain HttpDataSet#getHeaderContent()}不是名/值对象数组JSON异常。
 * 
 * @author datagear@163.com
 *
 */
public class HeaderContentNotNameValueObjArrayJsonException extends NotNameValueObjArrayJsonException
{
	private static final long serialVersionUID = 1L;

	public HeaderContentNotNameValueObjArrayJsonException(String json)
	{
		super(json);
	}

	public HeaderContentNotNameValueObjArrayJsonException(String json, String message)
	{
		super(json, message);
	}

	public HeaderContentNotNameValueObjArrayJsonException(String json, Throwable cause)
	{
		super(json, cause);
	}

	public HeaderContentNotNameValueObjArrayJsonException(String json, String message, Throwable cause)
	{
		super(json, message, cause);
	}
}
