/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 属性名称解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface PropertyNameResolver
{
	/**
	 * 解析属性名称。
	 * 
	 * @param context
	 * @param candidates
	 *            候选名称数组，字段名、属性关联表名、外键名称等，越靠前的越应该被优先使用。
	 * @return
	 */
	String resolve(PropertyNameContext context, String... candidates);
}
