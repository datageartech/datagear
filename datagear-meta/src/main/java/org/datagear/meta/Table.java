/*
 * Copyright 2018-2023 datagear.tech
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
	 * 获取指定名称的列索引。
	 * 
	 * @param name
	 * @return {@code -1}表示没有找到
	 */
	public int getColumnIndex(String name)
	{
		int re = -1;

		if (this.columns == null)
			return re;

		for (int i = 0; i < this.columns.length; i++)
		{
			Column column = this.columns[i];
			String myName = column.getName();

			// 优先取准确列，其次取忽略大小写的列
			if (myName.equals(name))
			{
				re = i;
				break;
			}
			else if (myName.equalsIgnoreCase(name))
			{
				re = i;
			}
		}

		return re;
	}

	/**
	 * 获取指定索引的{@linkplain Column}。
	 * 
	 * @param index
	 * @return
	 */
	public Column getColumn(int index)
	{
		if (this.columns == null)
			throw new NullPointerException("no column");

		return this.columns[index];
	}

	/**
	 * 获取指定名称的{@linkplain Column}。
	 * 
	 * @param name
	 * @return 返回{@code null}表示没有
	 */
	public Column getColumn(String name)
	{
		Column re = null;

		if (this.columns == null)
			return re;

		for (Column column : this.columns)
		{
			String myName = column.getName();

			// 优先取准确列，其次取忽略大小写的列
			if (myName.equals(name))
			{
				re = column;
				break;
			}
			else if (myName.equalsIgnoreCase(name))
			{
				re = column;
			}
		}

		return re;
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

	/**
	 * 获取所有二进制列。
	 * 
	 * @return 返回空数组表示没有。
	 */
	public Column[] getBinaryColumns()
	{
		List<Column> re = new ArrayList<>(2);

		if (this.columns != null)
		{
			for (Column column : this.columns)
			{
				if (JdbcUtil.isBinaryType(column.getType()))
					re.add(column);
			}
		}

		return re.toArray(new Column[re.size()]);
	}

	/**
	 * 给定列是否是外键列。
	 * 
	 * @param name
	 * @return
	 */
	public boolean isImportKeyColumn(String name)
	{
		if (!this.hasImportKey())
			return false;

		for (ImportKey ik : this.importKeys)
		{
			String[] colNames = ik.getColumnNames();

			if (colNames != null)
			{
				for (String colName : colNames)
				{
					// 此处不应区分大小写
					if (name.equalsIgnoreCase(colName))
						return true;
				}
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", comment=" + getComment()
				+ ", columns=" + Arrays.toString(columns) + ", primaryKey=" + primaryKey + ", uniqueKeys="
				+ Arrays.toString(uniqueKeys) + ", importKeys=" + Arrays.toString(importKeys) + "]";
	}
}
