/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

/**
 * 关系类型。
 * 
 * @author datagear@163.com
 *
 */
public enum RelationType
{
	/** 一对一 */
	ONE_TO_ONE,

	/** 一对多 */
	ONE_TO_MANY,

	/** 多对一 */
	MANY_TO_ONE,

	/** 多对多 */
	MANY_TO_MANY
}