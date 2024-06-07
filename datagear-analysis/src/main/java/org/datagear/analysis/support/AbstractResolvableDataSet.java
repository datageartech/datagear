/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.NameAwareUtil;
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
	private static final long serialVersionUID = 1L;

	public AbstractResolvableDataSet()
	{
		super();
	}

	public AbstractResolvableDataSet(String id, String name)
	{
		super(id, name, Collections.emptyList());
	}

	public AbstractResolvableDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
	}

	@Override
	public DataSetResult getResult(DataSetQuery query) throws DataSetException
	{
		checkRequiredParamValues(query);
		ResolvedDataSetResult result = resolveResult(query, false);
		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(DataSetQuery query) throws DataSetException
	{
		checkRequiredParamValues(query);
		return resolveResult(query, true);
	}

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param resolveProperties
	 *            是否从数据中解析{@linkplain DataSetField}，如果为{@code true}，
	 *            应解析并设置{@linkplain ResolvedDataSetResult#setFields(List)}
	 * @return
	 * @throws DataSetException
	 */
	protected abstract ResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveProperties)
			throws DataSetException;

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param rawData
	 *            允许为{@code null}
	 * @param rawDataFields
	 *            允许为{@code null}，如果不为空，将与{@linkplain #getFields()}合并后作为解析基础，否则，仅以{@linkplain #getFields()}作为解析基础
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, Object rawData,
			List<DataSetField> rawDataFields) throws Throwable
	{
		List<DataSetField> fields = getFields();

		if (fields == null)
			fields = Collections.emptyList();

		if (rawDataFields != null && !rawDataFields.isEmpty())
			fields = mergeFields(rawDataFields, fields);

		return resolveResult(rawData, fields, query.getResultFetchSize(), query.getResultDataFormat());
	}

	/**
	 * 合并{@linkplain DataSetField}。
	 * <p>
	 * 将合并列表的{@linkplain DataSetField#getType()}、{@linkplain DataSetField#getLabel()}、
	 * {@linkplain DataSetField#getDefaultValue()}合并至基础列表里的同名项，多余项则直接添加，
	 * 同时根据{@code merge}里的排序对{@code dataSetProperties}重排，返回一个新的列表。
	 * </p>
	 * 
	 * @param base
	 *            允许为{@code null}，基础列表
	 * @param merge
	 *            允许为{@code null}，合并列表
	 * @return
	 */
	protected List<DataSetField> mergeFields(List<? extends DataSetField> base,
			List<? extends DataSetField> merge)
	{
		if (base == null)
			base = Collections.emptyList();
		if (merge == null)
			merge = Collections.emptyList();

		List<DataSetField> dps = new ArrayList<DataSetField>(base.size());
		for (DataSetField field : base)
			dps.add(field.clone());

		for (DataSetField dp : dps)
		{
			DataSetField mp = NameAwareUtil.find(merge, dp.getName());
			
			if(mp != null)
			{
				dp.setType(mp.getType());
				dp.setLabel(mp.getLabel());
				dp.setDefaultValue(mp.getDefaultValue());
				dp.setEvaluated(mp.isEvaluated());
				dp.setExpression(mp.getExpression());
			}
		}

		for (DataSetField mp : merge)
		{
			if (NameAwareUtil.find(dps, mp.getName()) == null)
				dps.add(mp);
		}

		final List<? extends DataSetField> mergedFinal = merge;

		dps.sort(new Comparator<DataSetField>()
		{
			@Override
			public int compare(DataSetField o1, DataSetField o2)
			{
				// 优先按照merged列表中的顺序重排
				int o1Idx = NameAwareUtil.findIndex(mergedFinal, o1.getName());
				int o2Idx = NameAwareUtil.findIndex(mergedFinal, o2.getName());

				if (o1Idx < 0)
					o1Idx = NameAwareUtil.findIndex(dps, o1.getName());
				if (o2Idx < 0)
					o2Idx = NameAwareUtil.findIndex(dps, o2.getName());

				return Integer.valueOf(o1Idx).compareTo(o2Idx);
			}
		});

		return dps;
	}
}
