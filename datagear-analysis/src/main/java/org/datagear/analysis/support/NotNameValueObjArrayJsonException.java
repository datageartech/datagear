/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
