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
