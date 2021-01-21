/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
