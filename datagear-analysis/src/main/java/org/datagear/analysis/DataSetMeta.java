/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.List;

/**
 * 数据集元信息。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetMeta
{
	@SuppressWarnings("unchecked")
	private List<ColumnMeta> columnMetas = Collections.EMPTY_LIST;

	public DataSetMeta()
	{
	}

	@SuppressWarnings("unchecked")
	public DataSetMeta(List<? extends ColumnMeta> columnMetas)
	{
		super();
		this.columnMetas = (List<ColumnMeta>) columnMetas;
	}

	public List<ColumnMeta> getColumnMetas()
	{
		return columnMetas;
	}

	@SuppressWarnings("unchecked")
	public void setColumnMetas(List<? extends ColumnMeta> columnMetas)
	{
		this.columnMetas = (List<ColumnMeta>) columnMetas;
	}

	/**
	 * 获取指定名称的{@linkplain ColumnMeta}。
	 * 
	 * @param name
	 * @return
	 */
	public ColumnMeta getColumnMeta(String name)
	{
		if (this.columnMetas == null)
			return null;

		for (ColumnMeta meta : this.columnMetas)
		{
			if (meta.getName().equals(name))
				return meta;
		}

		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnMetas=" + columnMetas + "]";
	}
}
