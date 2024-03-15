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
import org.datagear.analysis.DataSetResult;

/**
 * 不支持的数据集结果数据异常。
 * <p>
 * 当数据集结果数据对象不符合{@linkplain DataSetResult#getData()}所要求的类型时，将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedResultDataException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedResultDataException()
	{
		super();
	}

	public UnsupportedResultDataException(String message)
	{
		super(message);
	}

	public UnsupportedResultDataException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedResultDataException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
