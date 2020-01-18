/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataNameAndType;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetExport;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;

/**
 * 抽象{@linkplain DataSetFactory}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSetFactory extends AbstractIdentifiable implements DataSetFactory
{
	private List<DataSetProperty> properties;

	private List<DataSetParam> params;

	private List<DataSetExport> exports;

	public AbstractDataSetFactory()
	{
		super();
	}

	public AbstractDataSetFactory(String id, List<DataSetProperty> properties)
	{
		super(id);
		this.properties = properties;
	}

	@Override
	public List<DataSetProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<DataSetProperty> properties)
	{
		this.properties = properties;
	}

	@Override
	public DataSetProperty getProperty(String name)
	{
		return getDataNameAndTypeByName(this.properties, name);
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	@Override
	public List<DataSetParam> getParams()
	{
		return params;
	}

	public void setParams(List<DataSetParam> params)
	{
		this.params = params;
	}

	@Override
	public DataSetParam getParam(String name)
	{
		return getDataNameAndTypeByName(this.params, name);
	}

	public boolean hasExport()
	{
		return (this.exports != null && !this.exports.isEmpty());
	}

	@Override
	public List<DataSetExport> getExports()
	{
		return exports;
	}

	public void setExports(List<DataSetExport> exports)
	{
		this.exports = exports;
	}

	@Override
	public DataSetExport getExport(String name)
	{
		return getDataNameAndTypeByName(this.exports, name);
	}

	/**
	 * 获取输出值集。
	 * 
	 * @param meta
	 * @param datas
	 * @return
	 * @throws DataSetException
	 */
	protected Map<String, ?> getExportValues(DataSet dataSet) throws DataSetException
	{
		if (!hasExport())
			return null;

		Map<String, Object> exportValues = new HashMap<String, Object>();

		for (DataSetExport expt : this.exports)
		{
			Object value = expt.getExportValue(this, dataSet);
			exportValues.put(expt.getName(), value);
		}

		return exportValues;
	}

	/**
	 * 获取指定名称的{@linkplain DataSetParam}，找不到将抛出{@linkplain DataSetParamNotFountException}。
	 * 
	 * @param name
	 * @return
	 */
	public DataSetParam getParamNotNull(String name) throws DataSetParamNotFountException
	{
		DataSetParam dataSetParam = getParam(name);

		if (dataSetParam == null)
			throw new DataSetParamNotFountException(name);

		return dataSetParam;
	}

	/**
	 * 获取指定名称列表的{@linkplain DataSetParam}列表，找不到将抛出{@linkplain DataSetParamNotFountException}。
	 * 
	 * @param names
	 * @return
	 * @throws DataSetParamNotFountException
	 */
	public List<DataSetParam> getParamsNotNull(List<String> names) throws DataSetParamNotFountException
	{
		List<DataSetParam> dataSetParams = new ArrayList<DataSetParam>(names.size());

		for (String name : names)
			dataSetParams.add(getParamNotNull(name));

		return dataSetParams;
	}

	/**
	 * 获取指定名称的{@linkplain DataNameAndType}对象，没找到则返回{@code null}。
	 * 
	 * @param <T>
	 * @param list
	 *            允许为{@code null}
	 * @param name
	 * @return
	 */
	protected <T extends DataNameAndType> T getDataNameAndTypeByName(List<T> list, String name)
	{
		if (list != null)
		{
			for (T t : list)
			{
				if (t.getName().equals(name))
					return t;
			}
		}

		return null;
	}
}
