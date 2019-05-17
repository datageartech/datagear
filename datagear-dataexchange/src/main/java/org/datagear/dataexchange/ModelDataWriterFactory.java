/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * {@linkplain ModelDataWriter}工厂类。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelDataWriterFactory
{
	/**
	 * 获取指定{@linkplain Export}的{@linkplain ModelDataWriter}。
	 * 
	 * @param expt
	 * @return
	 */
	ModelDataWriter get(Export expt);
}
