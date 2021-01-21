/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.util.JdbcUtil;

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

	/** 导入外键 */
	private ImportKey[] importKeys;

	/** 表是否只读 */
	private boolean readonly = false;

	public Table()
	{
		super();
	}

	public Table(String name, String type)
	{
		this(name, type, null);
	}

	public Table(String name, String type, Column[] columns)
	{
		super(name, type);
		this.columns = (columns == null ? new Column[0] : columns);
	}

	/**
	 * 是否有列。
	 * <p>
	 * 对于某些NewSql数据库（比如Elasticsearch）可能存在没有定义列的表。
	 * </p>
	 * 
	 * @return
	 */
	public boolean hasColumn()
	{
		return (this.columns != null && this.columns.length > 0);
	}

	public Column[] getColumns()
	{
		return columns;
	}

	public void setColumns(Column[] columns)
	{
		this.columns = (columns == null ? new Column[0] : columns);
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

	public boolean hasImportKey()
	{
		return (this.importKeys != null && this.importKeys.length > 0);
	}

	public ImportKey[] getImportKeys()
	{
		return importKeys;
	}

	public void setImportKeys(ImportKey[] importKeys)
	{
		this.importKeys = importKeys;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * 获取指定名称的{@linkplain Column}。
	 * 
	 * @param name
	 * @return 返回{@code null}表示没有
	 */
	public Column getColumn(String name)
	{
		for (Column column : this.columns)
		{
			if (column.getName().equals(name))
				return column;
		}

		return null;
	}

	/**
	 * 获取指定名称的{@linkplain Column}数组。
	 * 
	 * @param names
	 * @return 返回元素{@code null}表示没有
	 */
	public Column[] getColumns(String... names)
	{
		Column[] columns = new Column[names.length];

		for (int i = 0; i < columns.length; i++)
			columns[i] = getColumn(names[i]);

		return columns;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", comment=" + getComment()
				+ ", columns=" + Arrays.toString(columns) + ", primaryKey=" + primaryKey + ", uniqueKeys="
				+ Arrays.toString(uniqueKeys) + ", importKeys=" + Arrays.toString(importKeys) + "]";
	}

	/**
	 * 获取所有二进制列。
	 * 
	 * @return 返回空数组表示没有。
	 */
	public static Column[] getBinaryColumns(Table table)
	{
		List<Column> bcs = new ArrayList<>(1);

		Column[] columns = table.getColumns();
		for (Column column : columns)
		{
			if (JdbcUtil.isBinaryType(column.getType()))
				bcs.add(column);
		}

		return bcs.toArray(new Column[bcs.size()]);
	}
}
