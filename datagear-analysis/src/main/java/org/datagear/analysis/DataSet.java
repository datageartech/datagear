/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;
import java.util.Map;

/**
 * 数据集。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSet
{
	/**
	 * 获取数据。
	 * 
	 * @return
	 */
	List<?> getDatas();

	/**
	 * 获取输出项值集合，返回{@code null}或空表示没有输出项值。
	 * 
	 * @return
	 */
	Map<String, ?> getExportValues();

	/**
	 * 获取{@linkplain #getDatas()}中单条数据指定名称的属性值。
	 * 
	 * @param data
	 * @param name
	 * @return
	 * @throws DataSetException
	 */
	Object getDataPropertyValue(Object data, String name) throws DataSetException;
}
