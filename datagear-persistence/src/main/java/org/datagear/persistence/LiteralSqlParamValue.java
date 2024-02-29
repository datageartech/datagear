/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.persistence;

import org.datagear.util.SqlParamValue;

/**
 * 字面值{@linkplain SqlParamValue}。
 * <p>
 * 此类实例的{@linkplain #getValue()}不是参数值，而是SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class LiteralSqlParamValue extends SqlParamValue
{
	public LiteralSqlParamValue()
	{
		super();
	}

	public LiteralSqlParamValue(String value, int type)
	{
		super(value, type);
	}

	@Override
	public String getValue()
	{
		return (String) super.getValue();
	}

	@Override
	public void setValue(Object value)
	{
		if (value != null && !(value instanceof String))
			throw new IllegalArgumentException("[value] must be a string");

		super.setValue(value);
	}
}
