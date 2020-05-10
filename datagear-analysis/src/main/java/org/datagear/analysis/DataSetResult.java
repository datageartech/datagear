/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 数据集结果。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetResult
{
	/**
	 * 获取数据列表。
	 * 
	 * @return
	 */
	List<?> getDatas();

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
