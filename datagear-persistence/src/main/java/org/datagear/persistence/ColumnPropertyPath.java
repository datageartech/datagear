/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.io.Serializable;

/**
 * 列对应的属性路径。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnPropertyPath implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 列名称 */
	private String columnName;

	/** 添加数据库标识符引用的列名称 */
	private String columnNameQuote;

	/** 列SQL类型 */
	private int columnSqlType;

	/** 是否Token列 */
	private boolean token;

	/** 是否是集合/数组的大小列 */
	private boolean sizeColumn;

	/** 属性路径 */
	private String propertyPath;

	public ColumnPropertyPath()
	{
		super();
	}

	public ColumnPropertyPath(String columnName, String columnNameQuote, int columnSqlType, boolean token,
			boolean sizeColumn, String propertyPath)
	{
		super();
		this.columnName = columnName;
		this.columnNameQuote = columnNameQuote;
		this.columnSqlType = columnSqlType;
		this.token = token;
		this.sizeColumn = sizeColumn;
		this.propertyPath = propertyPath;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public String getColumnNameQuote()
	{
		return columnNameQuote;
	}

	public void setColumnNameQuote(String columnNameQuote)
	{
		this.columnNameQuote = columnNameQuote;
	}

	public int getColumnSqlType()
	{
		return columnSqlType;
	}

	public void setColumnSqlType(int columnSqlType)
	{
		this.columnSqlType = columnSqlType;
	}

	public boolean isToken()
	{
		return token;
	}

	public void setToken(boolean token)
	{
		this.token = token;
	}

	public boolean isSizeColumn()
	{
		return sizeColumn;
	}

	public void setSizeColumn(boolean sizeColumn)
	{
		this.sizeColumn = sizeColumn;
	}

	public String getPropertyPath()
	{
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnName=" + columnName + ", columnNameQuote=" + columnNameQuote
				+ ", columnSqlType=" + columnSqlType + ", token=" + token + ", sizeColumn=" + sizeColumn
				+ ", propertyPath=" + propertyPath + "]";
	}
}
