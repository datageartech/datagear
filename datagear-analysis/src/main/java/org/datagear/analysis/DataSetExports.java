/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 数据集输出项列表。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetExports extends AbstractDelegatedList<DataSetExport>
{
	public DataSetExports()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public DataSetExports(List<? extends DataSetExport> delegatedList)
	{
		super((List<DataSetExport>) delegatedList);
	}

	/**
	 * 获取指定名称的{@linkplain DataSetExport}，未找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	public DataSetExport getByName(String name)
	{
		for (int i = 0, len = this.size(); i < len; i++)
		{
			DataSetExport dataSetExport = get(i);

			if (dataSetExport.getName().equals(name))
				return dataSetExport;
		}

		return null;
	}
}
