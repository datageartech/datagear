/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.io.Serializable;

/**
 * 查询结果列元信息。
 * 
 * @author datagear@163.com
 *
 */
public class QueryColumnMetaInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 列路径，比如："T0"."COLUMN_0" */
	private String columnPath;

	/** 列别名 */
	private String columnAlias;

	/** 列SQL类型 */
	private int columnSqlType;

	/** 是否Token列 */
	private boolean token;

	/** 是否是集合/数组的大小列 */
	private boolean sizeColumn;

	/** 属性路径 */
	private String propertyPath;

	public QueryColumnMetaInfo()
	{
		super();
	}

	public QueryColumnMetaInfo(String columnPath, String columnAlias, int columnSqlType, boolean token,
			boolean sizeColumn, String propertyPath)
	{
		super();
		this.columnPath = columnPath;
		this.columnAlias = columnAlias;
		this.columnSqlType = columnSqlType;
		this.token = token;
		this.sizeColumn = sizeColumn;
		this.propertyPath = propertyPath;
	}

	public String getColumnPath()
	{
		return columnPath;
	}

	public void setColumnPath(String columnPath)
	{
		this.columnPath = columnPath;
	}

	public String getColumnAlias()
	{
		return columnAlias;
	}

	public void setColumnAlias(String columnAlias)
	{
		this.columnAlias = columnAlias;
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
		return getClass().getSimpleName() + "[columnPath=" + columnPath + ", columnAlias=" + columnAlias
				+ ", columnSqlType=" + columnSqlType + ", token=" + token + ", sizeColumn=" + sizeColumn
				+ ", propertyPath=" + propertyPath + "]";
	}
}
