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

package org.datagear.analysis.form;

/**
 * {@linkplain FormProperty#getType()}枚举。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyType
{
	/** 字符串 */
	public static final String STRING = "STRING";

	/** 布尔值 */
	public static final String BOOLEAN = "BOOLEAN";

	/** 整数 */
	public static final String INTEGER = "INTEGER";

	/** 数值 */
	public static final String NUMBER = "NUMBER";

	/** 对象 */
	public static final String OBJECT = "OBJECT";

	/**
	 * 规范类型。
	 * 
	 * @param type
	 * @param dftValue
	 * @return
	 */
	public static String normalize(String type, String dftValue)
	{
		if (type == null)
			return dftValue;

		if (STRING.equalsIgnoreCase(type))
			return STRING;

		if (BOOLEAN.equalsIgnoreCase(type))
			return BOOLEAN;

		if (INTEGER.equalsIgnoreCase(type))
			return INTEGER;

		if (NUMBER.equalsIgnoreCase(type))
			return NUMBER;

		if (OBJECT.equalsIgnoreCase(type))
			return OBJECT;

		return dftValue;
	}
}