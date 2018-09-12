/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.io.Serializable;

import org.datagear.model.InstanceCreationException;
import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 默认模型。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultModel extends AbstractFeatured implements Model, Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 类型 */
	private Class<?> type;

	/** 属性集 */
	private Property[] properties;

	/** ID属性数组 */
	private Property[] idProperties;

	/** 唯一键数组 */
	private Property[][] uniqueProperties;

	/**
	 * 创建模型实例。
	 */
	public DefaultModel()
	{
		super();
	}

	/**
	 * 创建模型实例。
	 * 
	 * @param name
	 * @param type
	 */
	public DefaultModel(String name, Class<?> type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * 设置ID
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Class<?> getType()
	{
		return type;
	}

	/**
	 * 设置数据类型。
	 * 
	 * @param type
	 */
	public void setType(Class<?> type)
	{
		this.type = type;
	}

	@Override
	public boolean hasProperty()
	{
		return this.properties != null && this.properties.length > 0;
	}

	@Override
	public Property[] getProperties()
	{
		return properties;
	}

	/**
	 * 设置{@linkplain Property 属性}数组。
	 * 
	 * @param properties
	 */
	public void setProperties(Property[] properties)
	{
		this.properties = properties;
	}

	@Override
	public Property getProperty(String propertyName)
	{
		Property re = null;

		if (this.properties != null)
		{
			for (Property property : this.properties)
			{
				if (property.getName().equals(propertyName))
				{
					re = property;
					break;
				}
			}
		}

		return re;
	}

	@Override
	public Property getProperty(int index)
	{
		return this.properties[index];
	}

	@Override
	public boolean hasIdProperty()
	{
		return this.idProperties != null && this.idProperties.length > 0;
	}

	@Override
	public Property[] getIdProperties()
	{
		return this.idProperties;
	}

	/**
	 * 设置ID属性数组。
	 * 
	 * @param idProperties
	 */
	public void setIdProperties(Property[] idProperties)
	{
		this.idProperties = idProperties;
	}

	@Override
	public boolean hasUniqueProperty()
	{
		return this.uniqueProperties != null && this.uniqueProperties.length > 0;
	}

	@Override
	public Property[][] getUniqueProperties()
	{
		return uniqueProperties;
	}

	/**
	 * 设置唯一键属性数组。
	 * 
	 * @param uniqueProperties
	 */
	public void setUniqueProperties(Property[][] uniqueProperties)
	{
		this.uniqueProperties = uniqueProperties;
	}

	@Override
	public Object newInstance() throws InstanceCreationException
	{
		try
		{
			return this.type.newInstance();
		}
		catch (Exception e)
		{
			throw new InstanceCreationException(e);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultModel other = (DefaultModel) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}
}
