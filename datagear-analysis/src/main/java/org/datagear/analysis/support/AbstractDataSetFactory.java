/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetExport;
import org.datagear.analysis.DataSetExportValues;
import org.datagear.analysis.DataSetExports;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetMeta;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetParams;

/**
 * 抽象{@linkplain DataSetFactory}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSetFactory extends AbstractIdentifiable implements DataSetFactory
{
	private DataSetParams params;

	private DataSetExports exports;

	public AbstractDataSetFactory()
	{
		super();
	}

	public AbstractDataSetFactory(String id, DataSetParams params)
	{
		super(id);
		this.params = params;
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	@Override
	public DataSetParams getParams()
	{
		return params;
	}

	public void setParams(DataSetParams params)
	{
		this.params = params;
	}

	public boolean hasExport()
	{
		return (this.exports != null && !this.exports.isEmpty());
	}

	@Override
	public DataSetExports getExports()
	{
		return exports;
	}

	public void setExports(DataSetExports exports)
	{
		this.exports = exports;
	}

	/**
	 * 获取输出值集。
	 * 
	 * @param meta
	 * @param datas
	 * @return
	 * @throws DataSetException
	 */
	protected DataSetExportValues getExportValues(DataSetMeta meta, List<Map<String, ?>> datas) throws DataSetException
	{
		if (!hasExport())
			return null;

		DataSetExportValues exportValues = new DataSetExportValues();

		for (DataSetExport expt : this.exports)
		{
			Object value = expt.getExportValue(meta, datas);
			exportValues.put(expt.getName(), value);
		}

		return exportValues;
	}

	/**
	 * 获取指定名称列表的{@linkplain DataSetParam}列表，找不到将抛出{@linkplain DataSetParamNotFountException}。
	 * 
	 * @param names
	 * @return
	 * @throws DataSetParamNotFountException
	 */
	protected List<DataSetParam> getDataSetParamsNotNull(List<String> names) throws DataSetParamNotFountException
	{
		List<DataSetParam> dataSetParams = new ArrayList<DataSetParam>(names.size());

		for (String name : names)
			dataSetParams.add(getDataSetParamNotNull(name));

		return dataSetParams;
	}

	/**
	 * 获取指定名称的{@linkplain DataSetParam}，找不到将抛出{@linkplain DataSetParamNotFountException}。
	 * 
	 * @param name
	 * @return
	 */
	protected DataSetParam getDataSetParamNotNull(String name) throws DataSetParamNotFountException
	{
		DataSetParam dataSetParam = getDataSetParam(name);

		if (dataSetParam == null)
			throw new DataSetParamNotFountException(name);

		return dataSetParam;
	}

	/**
	 * 获取指定名称的{@linkplain DataSetParam}，找不到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	protected DataSetParam getDataSetParam(String name)
	{
		DataSetParam dataSetParam = null;

		DataSetParams dataSetParams = getParams();

		if (dataSetParams != null)
			dataSetParam = dataSetParams.getByName(name);

		return dataSetParam;
	}
}
