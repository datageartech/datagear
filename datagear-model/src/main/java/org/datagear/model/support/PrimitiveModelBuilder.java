/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.util.Map;

import org.datagear.model.InstanceCreationException;
import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 基本模型构建器。
 * 
 * @author datagear@163.com
 *
 */
public class PrimitiveModelBuilder
{
	public PrimitiveModelBuilder()
	{
		super();
	}

	/**
	 * 构建指定类型的基本模型。
	 * 
	 * @param type
	 * @return
	 */
	public Model build(Class<?> type)
	{
		String name = buildPrimitiveModelName(type);
		return new PrimitiveModel(name, type);
	}

	/**
	 * 构建指定名称和类型的基本模型。
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public Model build(String name, Class<?> type)
	{
		return new PrimitiveModel(name, type);
	}

	/**
	 * 构建基本模型名称。
	 * 
	 * @param type
	 * @return
	 */
	protected String buildPrimitiveModelName(Class<?> type)
	{
		return type.getSimpleName();
	}

	/**
	 * 基本模型。
	 * <p>
	 * 此类用于创建基本模型。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class PrimitiveModel implements Model
	{
		private String name;

		private Class<?> type;

		public PrimitiveModel()
		{
			super();
		}

		public PrimitiveModel(String name, Class<?> type)
		{
			super();
			this.name = name;
			this.type = type;
		}

		@Override
		public boolean hasFeature()
		{
			return false;
		}

		@Override
		public boolean hasFeature(Object key)
		{
			return false;
		}

		@Override
		public <T> T getFeature(Object key)
		{
			return null;
		}

		@Override
		public void setFeature(Object key)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setFeature(Object key, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T removeFeature(Object key)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<?, ?> getFeatures()
		{
			return null;
		}

		@Override
		public String getName()
		{
			return this.name;
		}

		@Override
		public Class<?> getType()
		{
			return this.type;
		}

		@Override
		public boolean hasProperty()
		{
			return false;
		}

		@Override
		public Property[] getProperties()
		{
			return null;
		}

		@Override
		public Property getProperty(String name)
		{
			return null;
		}

		@Override
		public Property getProperty(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasIdProperty()
		{
			return false;
		}

		@Override
		public Property[] getIdProperties()
		{
			return null;
		}

		@Override
		public boolean hasUniqueProperty()
		{
			return false;
		}

		@Override
		public Property[][] getUniqueProperties()
		{
			return null;
		}

		@Override
		public Object newInstance() throws InstanceCreationException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
		}
	}
}
