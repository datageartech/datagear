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

package org.datagear.util.sqlvalidator;

import java.io.Serializable;

/**
 * SQL单词。
 * 
 * @author datagear@163.com
 * @deprecated 参考{@linkplain SqlTokenParser}
 */
@Deprecated
public class SqlToken implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 类型：字符串
	 */
	public static final String TYPE_STRING = "STR";

	/**
	 * 类型：引用标识符
	 */
	public static final String TYPE_QUOTE_IDENTIFIER = "QI";

	/**
	 * 类型：双横杠行注释
	 */
	public static final String TYPE_DML_LINE_COMMENT = "DML_LC";

	/**
	 * 类型：双反斜杠行注释
	 */
	public static final String TYPE_DBS_LINE_COMMENT = "DBS_LC";

	/**
	 * 类型：块注释
	 */
	public static final String TYPE_BLOCK_COMMENT = "B_LC";

	/**
	 * 类型：逗号
	 */
	public static final String TYPE_COMMA = "COMMA";

	/**
	 * 类型：其他，可能是：关键字、标识符、函数名等等
	 */
	public static final String TYPE_OTHER = "OTHER";

	/** 类型 */
	private String type;

	/** 单词 */
	private String value;

	public SqlToken(String type, String value)
	{
		super();
		this.type = type;
		this.value = value;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + ", value=" + value + "]";
	}
}