/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.DataSetProperty;

/**
 * 抽象JSON数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonDataSet extends AbstractFmkTemplateDataSet
{
	public static final JsonDataSetSupport JSON_DATA_SET_SUPPORT = new JsonDataSetSupport();

	public AbstractJsonDataSet()
	{
		super();
	}

	public AbstractJsonDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	protected JsonDataSetSupport getJsonDataSetSupport()
	{
		return JSON_DATA_SET_SUPPORT;
	}
}
