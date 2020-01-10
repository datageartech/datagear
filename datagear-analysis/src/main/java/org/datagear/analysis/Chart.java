/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

/**
 * 图表。
 * 
 * @author datagear@163.com
 *
 */
public class Chart extends AbstractIdentifiable
{
	public RenderContext renderContext;

	private ChartPlugin<?> plugin;

	private ChartPropertyValues propertyValues;

	private DataSetFactory[] dataSetFactories;

	public Chart()
	{
		super();
	}

	public Chart(String id)
	{
		super(id);
	}

	public Chart(String id, RenderContext renderContext, ChartPlugin<?> plugin, ChartPropertyValues propertyValues,
			DataSetFactory[] dataSetFactories)
	{
		super(id);
		this.renderContext = renderContext;
		this.plugin = plugin;
		this.propertyValues = propertyValues;
		this.dataSetFactories = dataSetFactories;
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	public ChartPlugin<?> getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin<?> plugin)
	{
		this.plugin = plugin;
	}

	public ChartPropertyValues getPropertyValues()
	{
		return propertyValues;
	}

	public void setPropertyValues(ChartPropertyValues propertyValues)
	{
		this.propertyValues = propertyValues;
	}

	public DataSetFactory[] getDataSetFactories()
	{
		return dataSetFactories;
	}

	public void setDataSetFactories(DataSetFactory[] dataSetFactories)
	{
		this.dataSetFactories = dataSetFactories;
	}

	/**
	 * 获取数据集。
	 * 
	 * @param dataSetParamValues
	 * @return
	 * @throws DataSetException
	 */
	public DataSet[] getDataSets(DataSetParamValues dataSetParamValues) throws DataSetException
	{
		if (this.dataSetFactories == null || this.dataSetFactories.length == 0)
			return new DataSet[0];

		DataSet[] dataSets = new DataSet[this.dataSetFactories.length];

		for (int i = 0; i < this.dataSetFactories.length; i++)
		{
			DataSet dataSet = this.dataSetFactories[i].getDataSet(dataSetParamValues);
			dataSets[i] = dataSet;
		}

		return dataSets;
	}
}
