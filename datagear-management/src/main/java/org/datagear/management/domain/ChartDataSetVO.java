/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.io.Serializable;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetResult;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain ChartDataSet}值对象。
 * 
 * @author datagear@163.com
 *
 */
public class ChartDataSetVO extends ChartDataSet implements CloneableEntity, Serializable
{
	private static final long serialVersionUID = 1L;

	public ChartDataSetVO()
	{
		super();
	}

	public ChartDataSetVO(DataSet dataSet)
	{
		super(dataSet);
	}

	/**
	 * 设置{@linkplain SummaryDataSetEntity}，仅用于保存操作。
	 * 
	 * @param dataSetEntity
	 */
	public void setSummaryDataSetEntity(SummaryDataSetEntity dataSetEntity)
	{
		super.setDataSet(dataSetEntity);
	}

	@JsonIgnore
	@Override
	public DataSetResult getResult()
	{
		return super.getResult();
	}

	@Override
	public ChartDataSetVO clone()
	{
		ChartDataSetVO entity = new ChartDataSetVO();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
