/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Collections;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
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

	@SuppressWarnings("unchecked")
	public AbstractResolvableDataSet(String id, String name)
	{
		super(id, name, Collections.EMPTY_LIST);
	}

	public AbstractResolvableDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	@Override
	public DataSetResult getResult(DataSetQuery query) throws DataSetException
	{
		query = toNonNullDataSetQuery(query);

		List<DataSetProperty> properties = getProperties();

		if (properties == null || properties.isEmpty())
			throw new DataSetException("[getProperties()] must not be empty");

		ResolvedDataSetResult result = resolveResult(query, properties);

		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(DataSetQuery query)
			throws DataSetException
	{
		query = toNonNullDataSetQuery(query);
		return resolveResult(query, null);
	}

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param properties
	 *            允许为{@code null}/空，此时，应自动解析并设置返回结果的{@linkplain ResolvedDataSetResult#setProperties(List)}；
	 *            如果不为{@code null}/空，直接将{@code properties}作为解析数据依据， 使用它处理结果数据，
	 *            并设置为返回结果的{@linkplain ResolvedDataSetResult#setProperties(List)}
	 * @return
	 * @throws DataSetException
	 */
	protected abstract ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties) throws DataSetException;
}
