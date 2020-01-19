/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.ArrayList;
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

	/**
	 * 获取{@linkplain #getDataSigns()}的名称分隔字符串。
	 * <p>
	 * 如果{@linkplain #getDataSigns()}为{@code null}，则返回空字符串。
	 * </p>
	 * 
	 * @param splitter
	 * @return
	 */
	public String getDataSignNamesText(String splitter)
	{
		if (this.dataSigns == null)
			return "";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.dataSigns.size(); i++)
		{
			DataSign dataSign = this.dataSigns.get(i);

			if (i > 0)
				sb.append(splitter);

			sb.append(dataSign.getName());
		}

		return sb.toString();
	}

	/**
	 * 从{@linkplain ChartPlugin#getDataSigns()}查找并设置{@linkplain DataSign}列表。
	 * <p>
	 * 如果{@code chartPlugin}或{@code names}任一为{@code null}，此方法什么也不做。
	 * </p>
	 * 
	 * @param chartPlugin
	 * @param names
	 * @throws IllegalArgumentException
	 */
	public void setDataSigns(ChartPlugin<?> chartPlugin, String... names) throws IllegalArgumentException
	{
		List<DataSign> source = (chartPlugin == null ? null : chartPlugin.getDataSigns());
		setDataSigns(source, names);
	}

	/**
	 * 从源列表中查找并设置{@linkplain DataSign}列表。
	 * <p>
	 * 如果{@code source}或{@code names}任一为{@code null}，此方法什么也不做。
	 * </p>
	 * 
	 * @param source
	 * @param names
	 * @throws IllegalArgumentException
	 *             当{@code source}中找不到指定名称的元素时
	 */
	public void setDataSigns(List<DataSign> source, String... names) throws IllegalArgumentException
	{
		if (source == null || names == null)
			return;

		List<DataSign> dataSigns = new ArrayList<DataSign>();

		for (String name : names)
		{
			DataSign dataSign = null;

			for (DataSign myDataSign : source)
			{
				if (myDataSign.getName().equals(name))
				{
					dataSign = myDataSign;
					break;
				}
			}

			if (dataSign == null)
				throw new IllegalArgumentException(
						"No " + DataSign.class.getSimpleName() + " found for name '" + name + "'");
		}

		this.dataSigns = dataSigns;
	}
}
