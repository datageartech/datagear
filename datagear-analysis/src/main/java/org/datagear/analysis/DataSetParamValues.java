/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Map;

/**
 * 数据集参数值集合。
 * <p>
 * 此类表示{@linkplain DataSetFactory}创建{@linkplain DataSet}所需要的参数值集。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValues
{
	private Map<String, ?> values;

	public DataSetParamValues()
	{
	}

	public DataSetParamValues(Map<String, ?> values)
	{
		super();
		this.values = values;
	}

	public Map<String, ?> getValues()
	{
		return values;
	}

	public void setValues(Map<String, ?> values)
	{
		this.values = values;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String name)
	{
		return (T) (this.values == null ? null : this.values.get(name));
	}
}
