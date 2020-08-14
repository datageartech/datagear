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
	 * @return
	 * @throws DataSetException
	 */
	ResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException;
}
