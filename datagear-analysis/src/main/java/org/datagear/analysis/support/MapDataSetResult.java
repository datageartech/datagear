/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;

/**
 * 映射表{@linkplain DataSetResult}。
 * <p>
 * 它的{@linkplain #getDatas()}元素为映射表对象。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MapDataSetResult implements DataSetResult
{
	private List<Map<String, ?>> datas;

	public MapDataSetResult()
	{
		super();
	}

	public MapDataSetResult(List<Map<String, ?>> datas)
	{
		super();
		this.datas = datas;
	}

	@Override
	public List<Map<String, ?>> getDatas()
	{
		return datas;
	}

	public void setDatas(List<Map<String, ?>> datas)
	{
		this.datas = datas;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getDataPropertyValue(Object data, String name) throws DataSetException
	{
		if (data == null)
			return null;

		return ((Map<String, Object>) data).get(name);
	}
}
