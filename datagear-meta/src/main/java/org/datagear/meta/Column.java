/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta;

import java.io.Serializable;

/**
 * 列。
 * 
 * @author datagear@163.com
 *
 */
public class Column implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** JDBC类型，对应java.sql.Types中的值 */
	private int type;

	/** 数据源依赖的类型名称，参考"TYPE_NAME"说明 */
	private String typeName;

	/** 列大小，字符串长度或者数值总长度 */
	private int size = 0;

	/** 小数部分的位数，如果不是小数，值为0 */
	private int decimalDigits = 0;

	/** 是否允许为null */
	private boolean nullable = false;

	/** 描述 */
	private String comment;

	/** 默认值 */
	private String defaultValue = null;

	/** 是否自增长 */
	private boolean autoincrement = false;

	/** 可搜索类型 */
	private SearchableType searchableType;

	/*** 是否可用于排序的 */
	private boolean sortable = false;

	/** 列在表中的顺序 */
	private int position = 1;

	public Column()
	{
		super();
	}

	public Column(String name, int type)
	{
		super();
		this.name = name;
		this.type = type;
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

	public boolean hasTypeName()
	{
		return (this.typeName != null && !this.typeName.isEmpty());
	}

	public String getTypeName()
	{
		return typeName;
	}

	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

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

	public boolean hasComment()
	{
		return (this.comment != null && !this.comment.isEmpty());
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public boolean hasDefaultValue()
	{
		return (this.defaultValue != null && !this.defaultValue.isEmpty());
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

	public boolean hasSearchableType()
	{
		return (this.searchableType != null);
	}

	public SearchableType getSearchableType()
	{
		return searchableType;
	}

	public void setSearchableType(SearchableType searchableType)
	{
		this.searchableType = searchableType;
	}

	public boolean isSortable()
	{
		return sortable;
	}

	public void setSortable(boolean sortable)
	{
		this.sortable = sortable;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", typeName=" + typeName + ", size="
				+ size
				+ ", decimalDigits=" + decimalDigits + ", nullable=" + nullable + ", comment=" + comment
				+ ", defaultValue=" + defaultValue + ", autoincrement=" + autoincrement + ", searchableType="
				+ searchableType + ", sortable=" + this.sortable + ", position=" + this.position + "]";
	}
}
