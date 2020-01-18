/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 图表数据集工厂。
 * 
 * @author datagear@163.com
 *
 */
public class ChartDataSetFactory
{
	/** 数据集工厂 */
	private DataSetFactory dataSetFactory;

	/** 数据标记集 */
	private List<DataSign> dataSigns;

	public ChartDataSetFactory()
	{
		super();
	}

	public ChartDataSetFactory(DataSetFactory dataSetFactory, List<DataSign> dataSigns)
	{
		super();
		this.dataSetFactory = dataSetFactory;
		this.dataSigns = dataSigns;
	}

	public DataSetFactory getDataSetFactory()
	{
		return dataSetFactory;
	}

	public void setDataSetFactory(DataSetFactory dataSetFactory)
	{
		this.dataSetFactory = dataSetFactory;
	}

	public List<DataSign> getDataSigns()
	{
		return dataSigns;
	}

	public void setDataSigns(List<DataSign> dataSigns)
	{
		this.dataSigns = dataSigns;
	}
}
