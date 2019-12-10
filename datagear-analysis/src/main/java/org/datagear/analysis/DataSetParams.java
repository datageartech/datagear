/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 数据集参数集。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParams extends AbstractDelegatedList<DataSetParam>
{
	public DataSetParams()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public DataSetParams(List<? extends DataSetParam> dataSetParams)
	{
		super((List<DataSetParam>) dataSetParams);
	}

	/**
	 * 获取指定名称的{@linkplain DataSetParam}，未找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	public DataSetParam getByName(String name)
	{
		for (int i = 0, len = this.size(); i < len; i++)
		{
			DataSetParam dataSetParam = get(i);

			if (dataSetParam.getName().equals(name))
				return dataSetParam;
		}

		return null;
	}
}
