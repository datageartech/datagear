/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 数据集输出项值集合。
 * <p>
 * 此类表示{@linkplain DataSetFactory}创建的{@linkplain DataSet}可输出的值集。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetExportValues extends AbstractDelegatedMap<String, Object>
{
	public DataSetExportValues()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public DataSetExportValues(Map<String, ?> dataSetParamValues)
	{
		super((Map<String, Object>) dataSetParamValues);
	}
}
