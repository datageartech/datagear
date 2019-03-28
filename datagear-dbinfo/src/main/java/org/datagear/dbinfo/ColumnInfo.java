/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 表的列信息。
 * <p>
 * 类结构参考
 * {@linkplain DatabaseMetaData#getColumns(String, String, String, String)}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ColumnInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** JDBC类型，对应java.sql.Types中的值 */
	private int type;

	/** 数据源依赖的类型名称，参考"TYPE_NAME"说明 */
	private String typeName;

	/** 列大小，字符串长度或者数值总长度 */
	private int size;

	/** 小数部分的位数，如果不是小数，值为0 */
	private int decimalDigits;

	/** 是否允许为null */
	private boolean nullable;

	/** 描述 */
	private String comment;

	/** 默认值 */
	private String defaultValue;

	/** 是否自增长 */
	private boolean autoincrement;

	public ColumnInfo()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	/**
	 * 获取列大小，返回{@code <= 0}表示无此值。
	 * 
	 * @return
	 */
	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getDecimalDigits()
	{
		return decimalDigits;
	}

	public void setDecimalDigits(int decimalDigits)
	{
		this.decimalDigits = decimalDigits;
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public void setNullable(boolean nullable)
	{
		this.nullable = nullable;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public boolean isAutoincrement()
	{
		return autoincrement;
	}

	public void setAutoincrement(boolean autoincrement)
	{
		this.autoincrement = autoincrement;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", typeName=" + typeName + ", size="
				+ size + ", decimalDigits=" + decimalDigits + ", nullable=" + nullable + ", comment=" + comment
				+ ", defaultValue=" + defaultValue + ", autoincrement=" + autoincrement + "]";
	}

}
