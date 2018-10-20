/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.sql.SQLException;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端具体模型由属性值ID标识，且使用此接口进行转换。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ConcreteById extends PropertyFeature, PersistenceFeature
{
	/**
	 * 获取指定属性值ID对应的具体属性模型。
	 * 
	 * @param model
	 * @param property
	 * @param propertyValueId
	 * @return
	 * @throws SQLException
	 */
	Model from(Model model, Property property, Object[] propertyValueId) throws SQLException;
}
