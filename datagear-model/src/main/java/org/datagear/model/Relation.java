/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

/**
 * 关联关系。
 * <p>
 * <i>关联关系</i>描述属性与其所属对象的关联关系。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public enum Relation
{
	/**
	 * 组合。
	 * <p>
	 * 组合属性值对象没有独立的生命周期，随实体的销毁而销毁。
	 * </p>
	 */
	COMPOSITION,

	/**
	 * 聚合。
	 * <p>
	 * 聚合属性值对象有独立的生命周期，不受实体的影响。
	 * </p>
	 */
	AGGREGATION,

	/**
	 * 物主。
	 * <p>
	 * <i>物主</i>是反向的<i>组合</i>关系，实体随物主属性值实体的销毁而销毁。
	 * </p>
	 */
	OWNER
}
