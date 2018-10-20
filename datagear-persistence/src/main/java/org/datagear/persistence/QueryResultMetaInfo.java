/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.util.List;

import org.datagear.model.Model;

/**
 * 查询结果集元信息。
 * 
 * @author datagear@163.com
 *
 */
public class QueryResultMetaInfo
{
	/** 结果集对应的模型 */
	private Model model;

	/** 列结果集属性路径列表 */
	private List<ColumnPropertyPath> columnPropertyPaths;

	public QueryResultMetaInfo()
	{
		super();
	}

	public QueryResultMetaInfo(Model model, List<ColumnPropertyPath> columnPropertyPaths)
	{
		super();
		this.model = model;
		this.columnPropertyPaths = columnPropertyPaths;
	}

	public Model getModel()
	{
		return model;
	}

	public void setModel(Model model)
	{
		this.model = model;
	}

	public List<ColumnPropertyPath> getColumnPropertyPaths()
	{
		return columnPropertyPaths;
	}

	public void setColumnPropertyPaths(List<ColumnPropertyPath> columnPropertyPaths)
	{
		this.columnPropertyPaths = columnPropertyPaths;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [model=" + model + ", columnPropertyPaths=" + columnPropertyPaths + "]";
	}
}
