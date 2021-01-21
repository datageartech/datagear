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
 * 数据名、类型接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface DataNameType
{
	/**
	 * 获取名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取数据类型。
	 * 
	 * @return
	 */
	String getType();
}
