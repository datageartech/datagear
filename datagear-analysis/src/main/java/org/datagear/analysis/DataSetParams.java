/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 数据集参数集。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParams
{
	private List<DataSetParam> params;

	public DataSetParams()
	{
	}

	public DataSetParams(List<DataSetParam> params)
	{
		super();
		this.params = params;
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	public List<DataSetParam> getParams()
	{
		return params;
	}

	public void setParams(List<DataSetParam> params)
	{
		this.params = params;
	}

	/**
	 * 获取指定名称的{@linkplain DataSetParam}，未找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	public DataSetParam getByName(String name)
	{
		if (this.params == null)
			return null;

		for (DataSetParam param : this.params)
		{
			if (param.getName().equals(name))
				return param;
		}

		return null;
	}
}
