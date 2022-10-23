/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 名称、类型相关类接口。
 * 
 * @author datagear@163.com
 *
 */
public interface NameTypeAware extends NameAware
{
	/**
	 * 获取数据类型。
	 * 
	 * @return
	 */
	String getType();
}
