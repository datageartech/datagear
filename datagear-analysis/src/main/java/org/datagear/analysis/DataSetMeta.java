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

	public DataSetMeta(List<ColumnMeta> columnMetas)
	{
		super();
		this.columnMetas = columnMetas;
	}

	public List<ColumnMeta> getColumnMetas()
	{
		return columnMetas;
	}

	public void setColumnMetas(List<ColumnMeta> columnMetas)
	{
		this.columnMetas = columnMetas;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnMetas=" + columnMetas + "]";
	}
}
