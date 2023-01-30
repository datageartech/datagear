/*
 * Copyright 2018-2023 datagear.tech
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

/**
 * 数据值转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataValueConvertionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private Object source;

	private String type;

	public DataValueConvertionException(Object source, String type)
	{
		super("Convert from [" + source + "] to [" + type + "] is not supported");
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, String message)
	{
		super(message);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, Throwable cause)
	{
		super(cause);
		this.type = type;
		this.source = source;
	}

	public DataValueConvertionException(Object source, String type, String message, Throwable cause)
	{
		super(message, cause);
		this.type = type;
		this.source = source;
	}

	public Object getSource()
	{
		return source;
	}

	protected void setSource(Object source)
	{
		this.source = source;
	}

	public String getType()
	{
		return type;
	}

	protected void setType(String type)
	{
		this.type = type;
	}

}
