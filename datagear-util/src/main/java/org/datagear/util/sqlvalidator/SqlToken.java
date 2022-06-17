/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.io.Serializable;

/**
 * SQL单词。
 * 
 * @author datagear@163.com
 *
 */
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