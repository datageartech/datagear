/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 数据集选项。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetOption
{
	/** 结果数据最大返回数目 */
	private int resultDataMaxCount = -1;

	public DataSetOption()
	{
		super();
	}

	/**
	 * 获取结果数据最大返回数目。
	 * 
	 * @return {@code <0} 表示不限定数目
	 */
	public int getResultDataMaxCount()
	{
		return resultDataMaxCount;
	}

	public void setResultDataMaxCount(int resultDataMaxCount)
	{
		this.resultDataMaxCount = resultDataMaxCount;
	}
}
