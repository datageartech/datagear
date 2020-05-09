/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain ChartDataSet}值对象。
 * 
 * @author datagear@163.com
 *
 */
public class ChartDataSetVO extends ChartDataSet
{
	public ChartDataSetVO()
	{
		super();
	}

	public ChartDataSetVO(DataSet dataSet)
	{
		super(dataSet);
	}

	@JsonIgnore
	@Override
	public boolean isResultReady()
	{
		return super.isResultReady();
	}

	@JsonIgnore
	@Override
	public DataSetResult getResult()
	{
		return super.getResult();
	}

}
