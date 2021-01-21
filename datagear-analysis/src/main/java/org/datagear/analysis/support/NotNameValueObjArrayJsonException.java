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

import org.datagear.analysis.DataSetException;

/**
 * JSON字符串不是名/值数组格式异常。
 * 
 * @author datagear@163.com
 *
 */
public class NotNameValueObjArrayJsonException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String json;

	public NotNameValueObjArrayJsonException(String json)
	{
		super("The json must be name/value object array");
		this.json = json;
	}

	public NotNameValueObjArrayJsonException(String json, String message)
	{
		super(message);
		this.json = json;
	}

	public NotNameValueObjArrayJsonException(String json, Throwable cause)
	{
		super(cause);
		this.json = json;
	}

	public NotNameValueObjArrayJsonException(String json, String message, Throwable cause)
	{
		super(message, cause);
		this.json = json;
	}

	public String getJson()
	{
		return json;
	}

	protected void setJson(String json)
	{
		this.json = json;
	}
}
