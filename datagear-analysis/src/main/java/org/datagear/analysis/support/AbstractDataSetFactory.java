/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataSetFactory;
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
}
