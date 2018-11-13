/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * {@linkplain PropertyNameResolver}上下文对象。
 * 
 * @author datagear@163.com
 *
 */
public interface PropertyNameContext
{
	String getModelName();

	boolean isDuplicate(String propertyName);
}
