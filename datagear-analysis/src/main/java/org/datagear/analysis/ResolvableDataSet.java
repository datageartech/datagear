/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Map;

/**
 * 可解析{@linkplain DataSetResult}。
 * <p>
 * 调用{@linkplain #resolve(Map)}无需预先设置{@linkplain #getProperties()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ResolvableDataSet extends DataSet
{
	/**
	 * 解析{@linkplain ResolvedDataSetResult}。
	 * 
	 * @param paramValues
	 * @param dataSetOption
	 *            设置选项，允许为{@code null}
	 * @return
	 * @throws DataSetException
	 */
	ResolvedDataSetResult resolve(Map<String, ?> paramValues, DataSetOption dataSetOption) throws DataSetException;
}
