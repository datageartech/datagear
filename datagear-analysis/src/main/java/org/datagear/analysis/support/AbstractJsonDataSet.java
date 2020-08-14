/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;

/**
 * 抽象JSON数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonDataSet extends AbstractFmkTemplateDataSet implements ResolvableDataSet
{
	public static final JsonDataSetSupport JSON_DATA_SET_SUPPORT = new JsonDataSetSupport();

	public AbstractJsonDataSet()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public AbstractJsonDataSet(String id, String name)
	{
		super(id, name, Collections.EMPTY_LIST);
	}

	public AbstractJsonDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	protected JsonDataSetSupport getJsonDataSetSupport()
	{
		return JSON_DATA_SET_SUPPORT;
	}

	@Override
	public ResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException
	{
		DataSetResult result = getResult(paramValues);
		List<DataSetProperty> properties = getJsonDataSetSupport().resolveDataSetProperties(result.getData());

		return new ResolvedDataSetResult(result, properties);
	}
}
