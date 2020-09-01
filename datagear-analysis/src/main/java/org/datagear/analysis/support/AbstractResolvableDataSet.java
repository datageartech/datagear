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
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;

/**
 * 抽象{@linkplain ResolvableDataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractResolvableDataSet extends AbstractDataSet implements ResolvableDataSet
{
	public AbstractResolvableDataSet()
	{
		super();
	}

	public AbstractResolvableDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		List<DataSetProperty> properties = getProperties();

		if (properties == null || properties.isEmpty())
			throw new DataSetException("[getProperties()] must not be empty");

		ResolvedDataSetResult result = resolveResult(paramValues, properties);

		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException
	{
		return resolveResult(paramValues, getProperties());
	}

	/**
	 * 解析结果。
	 * 
	 * @param paramValues
	 * @param properties
	 *            允许为空，此时，应自动解析并设置返回结果的{@linkplain ResolvedDataSetResult#setProperties(List)}；
	 *            如果不为空，直接将{@code properties}作为解析数据依据， 使用它处理结果数据，
	 *            并设置为返回结果的{@linkplain ResolvedDataSetResult#setProperties(List)}
	 * @return
	 * @throws DataSetException
	 */
	protected abstract ResolvedDataSetResult resolveResult(Map<String, ?> paramValues, List<DataSetProperty> properties)
			throws DataSetException;
}
