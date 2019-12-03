/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 数据集工厂。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetFactory
{
	/**
	 * 获取{@linkplain DataSetParam}列表。
	 * <p>
	 * 返回{@code null}或者空列表表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<DataSetParam> getDataSetParams();

	/**
	 * 获取{@linkplain DataSet}。
	 * 
	 * @param dataSetParamValues
	 * @return
	 */
	DataSet getDataSet(DataSetParamValues dataSetParamValues);
}
