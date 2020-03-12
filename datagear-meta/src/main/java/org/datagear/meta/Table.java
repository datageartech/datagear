/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import java.util.Arrays;

/**
 * 表。
 * 
 * @author datagear@163.com
 *
 */
public class Table extends AbstractTable
{
	private static final long serialVersionUID = 1L;

	/** 列集 */
	private Column[] columns;

	/** 主键 */
	private PrimaryKey primaryKey;

	/** 唯一键 */
	private UniqueKey[] uniqueKeys;

	/** 外键 */
	private ForeignKey[] foreignKeys;

	public Table()
	{
		super();
	}

	public Table(String name, TableType type, Column[] columns)
	{
		super(name, type);
		this.columns = columns;
	}

	public Column[] getColumns()
	{
		return columns;
	}

	public void setColumns(Column[] columns)
	{
		this.columns = columns;
	}

	public boolean hasPrimaryKey()
	{
		return (this.primaryKey != null);
	}

	public PrimaryKey getPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	public boolean hasUniqueKey()
	{
		return (this.uniqueKeys != null && this.uniqueKeys.length > 0);
	}

	public UniqueKey[] getUniqueKeys()
	{
		return uniqueKeys;
	}

	public void setUniqueKeys(UniqueKey[] uniqueKeys)
	{
		this.uniqueKeys = uniqueKeys;
	}

	public boolean hasForeignKey()
	{
		return (this.foreignKeys != null && this.foreignKeys.length > 0);
	}

	public ForeignKey[] getForeignKeys()
	{
		return foreignKeys;
	}

	public void setForeignKeys(ForeignKey[] foreignKeys)
	{
		this.foreignKeys = foreignKeys;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", comment=" + getComment()
				+ ", columns=" + Arrays.toString(columns) + ", primaryKey=" + primaryKey + ", uniqueKeys="
				+ Arrays.toString(uniqueKeys) + ", foreignKeys=" + Arrays.toString(foreignKeys) + "]";
	}
}
