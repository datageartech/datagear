/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

/**
 * 关联关系存储位置枚举。
 * 
 * @author datagear@163.com
 *
 */
public enum PointType
{
	/**
	 * 关联关系存储在模型表内。
	 */
	MODEL,

	/**
	 * 关联关系存储在属性表内。
	 */
	PROPERTY,

	/**
	 * 关联关系存储在连接表中。
	 */
	JOIN
}
