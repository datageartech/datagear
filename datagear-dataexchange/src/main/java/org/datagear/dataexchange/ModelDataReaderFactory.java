/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * {@linkplain ModelDataReader}工厂类。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelDataReaderFactory
{
	/**
	 * 获取指定{@linkplain Import}的{@linkplain ModelDataReader}。
	 * 
	 * @param impt
	 * @return
	 */
	ModelDataReader get(Import impt);
}
