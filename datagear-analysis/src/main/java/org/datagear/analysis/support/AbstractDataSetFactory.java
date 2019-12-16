/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataSetFactory;
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
	private DataSetParams dataSetParams;

	public AbstractDataSetFactory()
	{
		super();
	}

	public AbstractDataSetFactory(String id, DataSetParams dataSetParams)
	{
		super(id);
		this.dataSetParams = dataSetParams;
	}

	@Override
	public DataSetParams getDataSetParams()
	{
		return dataSetParams;
	}

	public void setDataSetParams(DataSetParams dataSetParams)
	{
		this.dataSetParams = dataSetParams;
	}

	/**
	 * 获取指定名称列表的{@linkplain DataSetParam}列表，找不到将抛出{@linkplain DataSetParamNotFountException}。
	 * 
	 * @param names
	 * @return
	 * @throws DataSetParamNotFountException
	 */
	protected List<DataSetParam> getDataSetParam(List<String> names) throws DataSetParamNotFountException
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

		DataSetParams dataSetParams = getDataSetParams();

		if (dataSetParams != null)
			dataSetParam = dataSetParams.getByName(name);

		return dataSetParam;
	}
}
