/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.resource;

/**
 * 资源工厂。
 * <p>
 * 此类用于获取、释放资源。
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
