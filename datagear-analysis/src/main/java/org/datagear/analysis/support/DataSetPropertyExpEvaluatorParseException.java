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
 * {@linkplain DataSetPropertyExpEvaluator}解析异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyExpEvaluatorParseException extends DataSetPropertyExpEvaluatorException
{
	private static final long serialVersionUID = 1L;

	public DataSetPropertyExpEvaluatorParseException()
	{
		super();
	}

	public DataSetPropertyExpEvaluatorParseException(String message, String propertyName)
	{
		super(message, propertyName);
	}

	public DataSetPropertyExpEvaluatorParseException(String message, Throwable cause, String propertyName)
	{
		super(message, cause, propertyName);
	}

	public DataSetPropertyExpEvaluatorParseException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DataSetPropertyExpEvaluatorParseException(String message)
	{
		super(message);
	}

	public DataSetPropertyExpEvaluatorParseException(Throwable cause, String propertyName)
	{
		super(cause, propertyName);
	}

	public DataSetPropertyExpEvaluatorParseException(Throwable cause)
	{
		super(cause);
	}
}
