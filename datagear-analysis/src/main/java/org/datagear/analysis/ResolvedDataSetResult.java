/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 数据集解析结果。
 * 
 * @author datagear@163.com
 *
 */
public class ResolvedDataSetResult
{
	private DataSetResult result;

	private List<DataSetProperty> properties;

	public ResolvedDataSetResult()
	{
	}

	public ResolvedDataSetResult(DataSetResult result, List<DataSetProperty> properties)
	{
		super();
		this.result = result;
		this.properties = properties;
	}

	public DataSetResult getResult()
	{
		return result;
	}

	public void setResult(DataSetResult result)
	{
		this.result = result;
	}

	public List<DataSetProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<DataSetProperty> properties)
	{
		this.properties = properties;
	}
}
