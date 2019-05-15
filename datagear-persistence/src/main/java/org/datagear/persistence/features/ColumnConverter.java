/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.PropertyFeature;
import org.datagear.model.support.MU;
import org.datagear.persistence.PersistenceFeature;
import org.datagear.persistence.columnconverter.ColumnConverterException;

/**
 * 列值转换器。
 * <p>
 * 它可用于如下场景：
 * </p>
 * <ul>
 * <li>单元（{@linkplain MU#isSingleProperty(Property)}）、基本（
 * {@linkplain MU#isPrimitiveProperty(Property)}）属性：
 * <p>
 * 用于定义模型表内属性列值和属性值的转换逻辑。
 * </p>
 * </li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public interface ColumnConverter extends PropertyFeature, PersistenceFeature
{
	/**
	 * 将属性值转换为列值。
	 * 
	 * @param cn
	 * @param model
	 * @param property
	 * @param propValue
	 * @return
	 * @throws ColumnConverterException
	 */
	Object to(Connection cn, Model model, Property property, Object propValue) throws ColumnConverterException;

	/**
	 * 将列值转换为属性值。
	 * 
	 * @param cn
	 * @param rs
	 * @param row
	 * @param colIndex
	 * @param model
	 * @param property
	 * @return
	 * @throws ColumnConverterException
	 */
	Object from(Connection cn, ResultSet rs, int row, int colIndex, Model model, Property property)
			throws ColumnConverterException;
}
