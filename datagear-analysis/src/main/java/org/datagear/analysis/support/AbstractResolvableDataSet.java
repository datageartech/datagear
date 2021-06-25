/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Collections;
import java.util.Comparator;
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
		checkRequiredParamValues(query);

		List<DataSetProperty> properties = getProperties();

		if (properties == null || properties.isEmpty())
			throw new DataSetException("[this.getProperties()] must not be empty");

		ResolvedDataSetResult result = resolveResult(query, properties, false);

		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(DataSetQuery query) throws DataSetException
	{
		checkRequiredParamValues(query);

		List<DataSetProperty> properties = getProperties();

		return resolveResult(query, properties, true);
	}

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 *            是否从数据中解析{@linkplain DataSetProperty}，如果为{@code true}，将解析且合并{@code properties}参数，
	 *            并设置为{@linkplain ResolvedDataSetResult#setProperties(List)}；如果为{@code false}，
	 *            将把{@code properties}参数直接设置为{@linkplain ResolvedDataSetResult#setProperties(List)}
	 * @return
	 * @throws DataSetException
	 */
	protected abstract ResolvedDataSetResult resolveResult(DataSetQuery query,
			List<DataSetProperty> properties, boolean resolveProperties) throws DataSetException;

	/**
	 * 合并{@linkplain DataSetProperty}。
	 * <p>
	 * 将{@code merged}待合并项里的{@linkplain DataSetProperty#getType()}、{@linkplain DataSetProperty#getLabel()}、
	 * {@linkplain DataSetProperty#getDefaultValue()}合并至{@code dataSetProperties}里的同名项，
	 * 多余项则直接添加至{@code dataSetProperties}，另外，也会根据{@code merged}里的排序对{@code dataSetProperties}重排。
	 * </p>
	 * 
	 * @param dataSetProperties
	 *            必须是可编辑的列表
	 * @param merged
	 *            待合并项，允许为{@code null}
	 * @return
	 */
	protected void mergeDataSetProperties(List<? extends DataSetProperty> dataSetProperties,
			List<? extends DataSetProperty> merged)
	{
		if (merged == null)
			merged = Collections.emptyList();

		@SuppressWarnings("unchecked")
		List<DataSetProperty> dps = (List<DataSetProperty>) dataSetProperties;

		for (DataSetProperty dp : dps)
		{
			DataSetProperty mp = getDataNameTypeByName(merged, dp.getName());
			
			if(mp != null)
			{
				dp.setType(mp.getType());
				dp.setLabel(mp.getLabel());
				dp.setDefaultValue(mp.getDefaultValue());
			}
		}

		for (DataSetProperty mp : merged)
		{
			if (getDataNameTypeByName(dps, mp.getName()) == null)
				dps.add(mp);
		}

		final List<? extends DataSetProperty> mergedFinal = merged;

		dps.sort(new Comparator<DataSetProperty>()
		{
			@Override
			public int compare(DataSetProperty o1, DataSetProperty o2)
			{
				// 优先按照merged列表中的顺序重排
				int o1Idx = getDataNameTypeIndexByName(mergedFinal, o1.getName());
				int o2Idx = getDataNameTypeIndexByName(mergedFinal, o2.getName());

				if (o1Idx < 0)
					o1Idx = getDataNameTypeIndexByName(dps, o1.getName());
				if (o2Idx < 0)
					o2Idx = getDataNameTypeIndexByName(dps, o2.getName());

				return Integer.valueOf(o1Idx).compareTo(o2Idx);
			}
		});
	}
}
