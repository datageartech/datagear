/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

/**
 * 模型表映射。
 * <p>
 * 模型表映射对应的属性端只能是基本模型、复合模型。
 * </p>
 * <p>
 * 基本模型：模型表内直接包含基本属性对应的字段；复合模型：模型表内仅包含复合属性的关联字段。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ModelTableMapper extends Mapper
{
	/**
	 * 是否是基本属性映射。
	 * 
	 * @return
	 */
	boolean isPrimitivePropertyMapper();

	/**
	 * 获取基本属性列名称（{@linkplain #isPrimitivePropertyMapper()}为{@code true}时起作用）。
	 * 
	 * @return
	 */
	String getPrimitiveColumnName();

	/**
	 * 获取属性端外键属性名称（{@linkplain #isPrimitivePropertyMapper()}为{@code false}时起作用）。
	 * <p>
	 * 属性端外键属性用于对应{@linkplain #getPropertyKeyColumnNames()}字段，通常是ID属性，也可能是唯一键属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getPropertyKeyPropertyNames();

	/**
	 * 获取属性端外键列名称（{@linkplain #isPrimitivePropertyMapper()}为{@code false}时起作用）。
	 * <p>
	 * 属性端外键用于存储和关联属性端数据对象，如果对应属性端模型的属性是复合属性，那么此外键名称按顺序对应那个复合属性的展开属性。
	 * </p>
	 * 
	 * @return
	 */
	String[] getPropertyKeyColumnNames();

	/**
	 * 是否有模型端排序字段（{@linkplain #isPrimitivePropertyMapper()}为{@code false}时起作用）。
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
}
