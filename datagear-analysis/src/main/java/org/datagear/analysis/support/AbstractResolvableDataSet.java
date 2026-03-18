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

	protected static final int START_RESOLVE_FIELDS_DEPTH = 0;

	protected static final int END_RESOLVE_FIELDS_DEPTH = 3;

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
	 * @param resolveFields
	 *            是否从数据中解析{@linkplain DataSetField}，如果为{@code true}，
	 *            应解析并设置{@linkplain ResolvedDataSetResult#setFields(List)}
	 * @return
	 * @throws DataSetException
	 */
	protected abstract ResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveFields)
			throws DataSetException;

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param rawResult
	 * @param rawDataFields
	 *            允许为{@code null}，如果不为空，将与{@linkplain #getFields()}合并后作为解析基础，否则，仅以{@linkplain #getFields()}作为解析基础
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, DataSetResult rawResult,
			List<DataSetField> rawDataFields) throws Throwable
	{
		List<DataSetField> fields = getFields();

		if (fields == null)
			fields = Collections.emptyList();

		if (rawDataFields != null && !rawDataFields.isEmpty())
			fields = mergeFields(rawDataFields, fields);

		return resolveResult(rawResult, fields, query.getResultFetchSize(), query.getResultDataFormat());
	}

	/**
	 * 合并{@linkplain DataSetField}。
	 * <p>
	 * 将合并列表的{@linkplain DataSetField#getType()}、{@linkplain DataSetField#getLabel()}、
	 * {@linkplain DataSetField#getDefaultValue()}合并至基础列表里的同名项，多余项则直接添加，
	 * 同时根据{@code merge}里的排序对{@code base}重排，返回一个新的列表。
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

		List<DataSetField> dfs = new ArrayList<DataSetField>(base.size());
		for (DataSetField field : base)
			dfs.add(field.clone());

		for (DataSetField df : dfs)
		{
			DataSetField mp = NameAwareUtil.find(merge, df.getName());
			
			if(mp != null)
			{
				df.setType(mp.getType());
				df.setFullname(mp.getFullname());
				df.setLabel(mp.getLabel());
				df.setDefaultValue(mp.getDefaultValue());
				df.setEvaluated(mp.isEvaluated());
				df.setExpression(mp.getExpression());
				df.setArray(mp.isArray());

				if (mp.getFields() != null)
					df.setFields(mergeFields(df.getFields(), mp.getFields()));
			}
		}

		for (DataSetField mf : merge)
		{
			if (NameAwareUtil.find(dfs, mf.getName()) == null)
				dfs.add(mf);
		}

		final List<? extends DataSetField> mergedFinal = merge;

		dfs.sort(new Comparator<DataSetField>()
		{
			@Override
			public int compare(DataSetField df1, DataSetField df2)
			{
				// 优先按照merged列表中的顺序重排
				int o1Idx = NameAwareUtil.findIndex(mergedFinal, df1.getName());
				int o2Idx = NameAwareUtil.findIndex(mergedFinal, df2.getName());

				if (o1Idx < 0)
					o1Idx = NameAwareUtil.findIndex(dfs, df1.getName());
				if (o2Idx < 0)
					o2Idx = NameAwareUtil.findIndex(dfs, df2.getName());

				return Integer.valueOf(o1Idx).compareTo(o2Idx);
			}
		});

		return dfs;
	}
}
