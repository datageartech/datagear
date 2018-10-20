/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import org.datagear.model.PropertyAccessException;

/**
 * 属性值访问器。
 * 
 * @author datagear@163.com
 *
 */
public interface PropertyValueAccessor
{
	/**
	 * 获取此属性值。
	 * 
	 * @param obj
	 * @return
	 * @throws PropertyAccessException
	 */
	Object get(Object obj) throws PropertyAccessException;

	/**
	 * 设置此属性值。
	 * 
	 * @param obj
	 * @param propertyValue
	 * @throws PropertyAccessException
	 */
	void set(Object obj, Object propertyValue) throws PropertyAccessException;
}
