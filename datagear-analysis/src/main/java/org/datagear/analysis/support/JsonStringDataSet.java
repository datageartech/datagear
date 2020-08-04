/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;

/**
 * JSON字符串数据集。
 * 
 * @author datagear@163.com
 *
 */
public class JsonStringDataSet extends AbstractJsonDataSet
{
	private String json;

	public JsonStringDataSet()
	{
		super();
	}

	public JsonStringDataSet(String id, String name, List<DataSetProperty> properties, String json)
	{
		super(id, name, properties);
		this.json = json;
	}

	public String getJson()
	{
		return json;
	}

	public void setJson(String json)
	{
		this.json = json;
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		return null;
	}
}
