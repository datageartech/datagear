/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

/**
 * 关联表映射。
 * <p>
 * 关联表映射对应的模型端模型和属性端模型都必须是复合模型。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface JoinTableMapper extends Mapper
{
	/**
	 * 获取关联表名称。
	 * 
	 * @return
	 */
	String getJoinTableName();

	/**
	 * 获取模型端外键属性名称。
	 * <p>
	 * 模型端外键属性用于对应{@linkplain #getModelKeyColumnNames()}字段，通常是ID属性，也可能是唯一值属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getModelKeyPropertyNames();

	/**
	 * 获取模型端外键列名称。
	 * <p>
	 * 模型端外键用于存储和关联模型端模型数据对象，如果对应模型端模型的属性是复合属性，那么此外键名称按顺序对应那个复合属性的展开属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getModelKeyColumnNames();

	/**
	 * 是否有模型端排序字段。
	 * 
	 * @return
	 */
	boolean hasModelOrderColumn();

	/**
	 * 获取模型端排序字段名称（{@linkplain #hasModelOrderColumn()}为{@code true}时起作用）。
	 * 
	 * @return
	 */
	String getModelOrderColumnName();

	/**
	 * 获取属性端外键属性名称。
	 * <p>
	 * 属性端外键属性用于对应{@linkplain #getPropertyKeyColumnNames()}字段，通常是ID属性，也可能是唯一值属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getPropertyKeyPropertyNames();

	/**
	 * 获取属性端外键列名称。
	 * <p>
	 * 属性端外键用于存储和关联属性端模型数据对象，如果对应属性端模型的属性是复合属性，那么此外键名称按顺序对应那个复合属性的展开属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getPropertyKeyColumnNames();

	/**
	 * 是否有属性端排序字段。
	 * 
	 * @return
	 */
	boolean hasPropertyOrderColumn();

	/**
	 * 获取属性端排序字段名称（{@linkplain #hasPropertyOrderColumn()}为{@code true}时起作用）。
	 * 
	 * @return
	 */
	String getPropertyOrderColumnName();
}
