/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Map;
import java.util.Set;

/**
 * 图表数据集。
 * <p>
 * 此类描述图表关联的某个{@linkplain DataSet}以及对其{@linkplain DataSet#getProperties()}设置的对应标记，这些标记通常是{@linkplain DataSign#getName()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartDataSet
{
	/** 数据集 */
	private DataSet dataSet;

	/** 数据集属性-标记映射表 */
	private Map<String, Set<String>> propertySigns;

	public ChartDataSet()
	{
		super();
	}

	public ChartDataSet(DataSet dataSet)
	{
		super();
		this.dataSet = dataSet;
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

	public void setDataSet(DataSet dataSet)
	{
		this.dataSet = dataSet;
	}

	public boolean hasPropertySign()
	{
		return (this.propertySigns != null && !this.propertySigns.isEmpty());
	}

	public Map<String, Set<String>> getPropertySigns()
	{
		return propertySigns;
	}

	public void setPropertySigns(Map<String, Set<String>> propertySigns)
	{
		this.propertySigns = propertySigns;
	}
}
