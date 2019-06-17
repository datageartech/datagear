/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 资源工厂。
 * <p>
 * {@linkplain DataExchangeService}使用它获取、释放数据交换资源。
 * </p>
 * <p>
 * 数据交换的某些资源（比如数据库连接、IO流）需要在{@linkplain DataExchangeService#exchange(DataExchange)}中初始化，
 * 而非在构建{@linkplain DataExchange}时，此类即用于支持这种场景。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface ResourceFactory<T>
{
	/**
	 * 获取资源。
	 * 
	 * @return
	 * @throws Exception
	 */
	T get() throws Exception;

	/**
	 * 释放资源。
	 * 
	 * @param resource
	 * @throws Exception
	 */
	void release(T resource) throws Exception;
}
