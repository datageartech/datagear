/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetExportValues;
import org.datagear.analysis.DataSetMeta;

/**
 * 简单{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDataSet implements DataSet
{
	private DataSetMeta meta;

	private List<?> datas;

	private DataSetExportValues exportValues;

	public SimpleDataSet()
	{
		super();
	}

	public SimpleDataSet(DataSetMeta meta, List<?> datas)
	{
		super();
		this.meta = meta;
		this.datas = datas;
	}

	@Override
	public DataSetMeta getMeta()
	{
		return meta;
	}

	public void setMeta(DataSetMeta meta)
	{
		this.meta = meta;
	}

	@Override
	public List<?> getDatas()
	{
		return datas;
	}

	public void setDatas(List<?> datas)
	{
		this.datas = datas;
	}

	public boolean hasExportValue()
	{
		return (this.exportValues != null && !this.exportValues.isEmpty());
	}

	@Override
	public DataSetExportValues getExportValues()
	{
		return exportValues;
	}

	public void setExportValues(DataSetExportValues exportValues)
	{
		this.exportValues = exportValues;
	}

}
