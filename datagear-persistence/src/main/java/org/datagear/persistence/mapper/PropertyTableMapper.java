/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.mapper;

/**
 * 属性表映射。
 * <p>
 * 属性表映射对应的属性端模型只能是基本模型、复合模型，而模型端模型只能是复合模型。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface PropertyTableMapper extends Mapper
{
	/**
	 * 是否是基本属性映射。
	 * 
	 * @return
	 */
	boolean isPrimitivePropertyMapper();

	/**
	 * 获取基本属性表名称（{@linkplain #isPrimitivePropertyMapper()}为{@code true}时起作用）。
	 * 
	 * @return
	 */
	String getPrimitiveTableName();

	/**
	 * 获取基本属性列名称（{@linkplain #isPrimitivePropertyMapper()}为{@code true}时起作用）。
	 * 
	 * @return
	 */
	String getPrimitiveColumnName();

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
