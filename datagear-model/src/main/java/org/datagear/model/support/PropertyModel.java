/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 属性模型信息。
 * <p>
 * 此类描述特定属性模型（{@linkplain Property#getModel()}中的某个）的信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyModel
{
	/** 属性 */
	private Property property;

	/** 属性的模型位置 */
	private int index;

	/** 属性模型 */
	private Model model;

	public PropertyModel()
	{
		super();
	}

	public PropertyModel(Property property, int index, Model model)
	{
		super();
		this.property = property;
		this.index = index;
		this.model = model;
	}

	public Property getProperty()
	{
		return property;
	}

	public void setProperty(Property property)
	{
		this.property = property;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public Model getModel()
	{
		return model;
	}

	public void setModel(Model model)
	{
		this.model = model;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [property=" + property + ", index=" + index + ", model=" + model + "]";
	}

	/**
	 * 构建{@linkplain PropertyModel}。
	 * 
	 * @param property
	 * @param propertyModel
	 * @return
	 */
	public static PropertyModel valueOf(Property property, Model propertyModel)
	{
		int myIndex = MU.getPropertyModelIndex(property, propertyModel);

		return new PropertyModel(property, myIndex, MU.getPropertyModel(property, myIndex));
	}

	/**
	 * 构建{@linkplain PropertyModel}。
	 * 
	 * @param property
	 * @param propertyValue
	 * @return
	 */
	public static PropertyModel valueOf(Property property, Object propertyValue)
	{
		int myIndex = MU.getPropertyModelIndex(property, propertyValue);

		return new PropertyModel(property, myIndex, MU.getPropertyModel(property, myIndex));
	}

	/**
	 * 构建{@linkplain PropertyModel}。
	 * 
	 * @param property
	 * @param propertyModelIndex
	 * @return
	 */
	public static PropertyModel valueOf(Property property, int propertyModelIndex)
	{
		return new PropertyModel(property, propertyModelIndex, MU.getPropertyModel(property, propertyModelIndex));
	}

	/**
	 * 构建{@linkplain PropertyModel}数组。
	 * 
	 * @param property
	 * @return
	 */
	public static PropertyModel[] valueOf(Property property)
	{
		Model[] pmodels = property.getModels();

		PropertyModel[] propertyModels = new PropertyModel[pmodels.length];

		for (int i = 0; i < pmodels.length; i++)
		{
			propertyModels[i] = new PropertyModel(property, i, pmodels[i]);
		}

		return propertyModels;
	}
}
