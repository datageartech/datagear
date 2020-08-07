/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 数据集结果。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResult
{
	/** 结果数据对象 */
	private Object data;

	public DataSetResult()
	{
		super();
	}

	public DataSetResult(Object data)
	{
		super();
		this.data = data;
	}

	/**
	 * 获取数据对象。
	 * <p>
	 * 数据对象应是所属{@linkplain DataSet#getProperties()}所描述结构的普通JavaBean、
	 * {@linkplain Map}对象，或者是它们的数组、集合。
	 * </p>
	 * 
	 * @return 数据对象，为{@code null}表示无数据
	 */
	public Object getData()
	{
		return this.data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}
}
