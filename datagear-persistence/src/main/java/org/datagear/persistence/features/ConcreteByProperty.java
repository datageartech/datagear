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
 * 属性端具体模型标识属性名。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain ManyToOne}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConcreteByProperty implements PropertyFeature, PersistenceFeature
{
	private String[] byProperties;

	private ConcreteByPropertyConverter converter;

	public ConcreteByProperty()
	{
		super();
	}

	public ConcreteByProperty(String byProperty, ConcreteByPropertyConverter converter)
	{
		super();
		this.byProperties = new String[] { byProperty };
		this.converter = converter;
	}

	public ConcreteByProperty(String[] byProperties, ConcreteByPropertyConverter converter)
	{
		super();
		this.byProperties = byProperties;
		this.converter = converter;
	}

	public String[] getByProperties()
	{
		return byProperties;
	}

	public void setByProperties(String... byProperties)
	{
		this.byProperties = byProperties;
	}

	public ConcreteByPropertyConverter getConverter()
	{
		return converter;
	}

	public void setConverter(ConcreteByPropertyConverter converter)
	{
		this.converter = converter;
	}

	/**
	 * 属性端具体模型列转换器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface ConcreteByPropertyConverter extends PropertyFeature, PersistenceFeature
	{
		/**
		 * 由标识属性值获取属性的具体模型。
		 * 
		 * @param model
		 * @param property
		 * @param identifyProperties
		 * @param identifyPropertyValues
		 * @return
		 * @throws SQLException
		 */
		Model from(Model model, Property property, Property[] identifyProperties, Object[] identifyPropertyValues)
				throws SQLException;
	}
}
