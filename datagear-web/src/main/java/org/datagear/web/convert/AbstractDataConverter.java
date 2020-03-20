/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.core.convert.ConversionService;

/**
 * 抽象数据转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataConverter
{
	protected static final ArrayElementKeyComparator ARRAY_ELEMENT_KEY_COMPARATOR = new ArrayElementKeyComparator();

	private ConversionService conversionService;

	private Map<Class<?>, Class<?>> instanceTypeMap = new HashMap<>();

	public AbstractDataConverter()
	{
		super();
		initDefaultInstanceTypeMap();
	}

	public AbstractDataConverter(ConversionService conversionService)
	{
		super();
		this.conversionService = conversionService;
		initDefaultInstanceTypeMap();
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	public Map<Class<?>, Class<?>> getInstanceTypeMap()
	{
		return instanceTypeMap;
	}

	public void setInstanceTypeMap(Map<Class<?>, Class<?>> instanceTypeMap)
	{
		this.instanceTypeMap = instanceTypeMap;
	}

	/**
	 * 初始化默认抽象集合类映射。
	 */
	protected void initDefaultInstanceTypeMap()
	{
		instanceTypeMap.put(Collection.class, ArrayList.class);
		instanceTypeMap.put(AbstractCollection.class, ArrayList.class);

		instanceTypeMap.put(Deque.class, LinkedList.class);
		instanceTypeMap.put(BlockingDeque.class, LinkedBlockingDeque.class);

		instanceTypeMap.put(List.class, ArrayList.class);
		instanceTypeMap.put(AbstractList.class, ArrayList.class);
		instanceTypeMap.put(AbstractSequentialList.class, LinkedList.class);

		instanceTypeMap.put(Queue.class, LinkedList.class);
		instanceTypeMap.put(BlockingQueue.class, ArrayBlockingQueue.class);

		instanceTypeMap.put(Set.class, HashSet.class);
		instanceTypeMap.put(AbstractSet.class, HashSet.class);

		instanceTypeMap.put(NavigableSet.class, TreeSet.class);
		instanceTypeMap.put(SortedSet.class, TreeSet.class);
	}

	/**
	 * 将简单对象转换为指定类型的对象。
	 * 
	 * @param obj
	 * @param targetType
	 *            简单类型，不能是数组和复合类型
	 * @param propertyPath
	 *            允许为{@code null}
	 * @return
	 * @throws IllegalSourceValueException
	 */
	@SuppressWarnings("unchecked")
	protected <T> T convertSimpleObj(Object obj, Class<T> targetType, String propertyPath)
			throws IllegalSourceValueException
	{
		// 提取单元素数组
		if (obj instanceof Object[])
		{
			int len = Array.getLength(obj);
			if (len == 0)
				obj = null;
			else if (len == 1)
				obj = Array.get(obj, 0);
		}

		if (obj instanceof String)
		{
			String str = (String) obj;

			if (isExpression(str))
				return (T) str;

			if ((str.isEmpty() && !String.class.equals(targetType)) || NULL_VALUE_PLACE_HOLDER.equals(str))
				obj = null;
		}

		try
		{
			return this.conversionService.convert(obj, targetType);
		}
		catch (Exception e)
		{
			throw new IllegalSourceValueException(propertyPath, obj, targetType);
		}
	}

	/**
	 * 将指定{@linkplain Map}包装为{@linkplain PropValueMap}。
	 * 
	 * @param map
	 * @return
	 */
	protected PropValueMap wrap(Map<?, ?> map)
	{
		if (map instanceof PropValueMap)
			return (PropValueMap) map;
		else
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> strKeyMap = (Map<String, ?>) map;

			PropValueMap pvm = new PropValueMap();

			for (Map.Entry<String, ?> entry : strKeyMap.entrySet())
			{
				String key = entry.getKey();
				Object value = entry.getValue();

				PropertyPath propertyPath = PropertyPath.valueOf(key);

				PropValueMap parent = pvm;

				for (int i = 0, len = propertyPath.length(); i < len; i++)
				{
					PropertyPath sub = propertyPath.sub(i);

					if (i == len - 1)
					{
						parent.put(sub, value);
					}
					else
					{
						PropValueMap tmp = (PropValueMap) parent.get(sub);

						if (tmp == null)
						{
							tmp = new PropValueMap();
							parent.put(sub, tmp);
						}

						parent = tmp;
					}
				}
			}

			return pvm;
		}
	}

	/**
	 * 创建类数组实例。
	 * 
	 * @param type
	 * @param length
	 *            数组长度
	 * @return
	 */
	protected <T> T[] createArrayInstance(Class<? extends T> type, int length) throws ConverterException
	{
		type = getInstanceType(type);

		@SuppressWarnings("unchecked")
		T[] re = (T[]) Array.newInstance(type, length);

		return re;
	}

	/**
	 * 创建类实例。
	 * 
	 * @param type
	 * @return
	 */
	protected <T> T createInstance(Class<? extends T> type) throws ConverterException
	{
		type = getInstanceType(type);

		try
		{
			return type.newInstance();
		}
		catch (Exception e)
		{
			throw new ConverterException(e);
		}
	}

	/**
	 * 获取指定类型的实例类型。
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> Class<? extends T> getInstanceType(Class<? extends T> type)
	{
		if (type.isArray())
		{
			Class<?> componentType = getInstanceType(type.getComponentType());
			Class<? extends T> instanceType = (Class) createArrayInstance(componentType, 0).getClass();

			return instanceType;
		}

		int mod = type.getModifiers();

		if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod))
			return type;

		Class<? extends T> instanceType = (Class<? extends T>) this.instanceTypeMap.get(type);

		if (instanceType == null)
			throw new ConverterException("No instance type found for type [" + type.getName() + "]");

		return getInstanceType(instanceType);
	}

	/**
	 * 比较{@code "[xxx]"}格式字符串的大小。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class ArrayElementKeyComparator implements Comparator<Map.Entry<PropertyPath, ?>>
	{
		public ArrayElementKeyComparator()
		{
			super();
		}

		@Override
		public int compare(Map.Entry<PropertyPath, ?> entry1, Map.Entry<PropertyPath, ?> entry2)
		{
			String o1 = entry1.getKey().toString(), o2 = entry2.getKey().toString();

			if (o1 == null && o2 == null)
				return 0;
			else if (o1 == null)
				return -1;
			else if (o2 == null)
				return 1;
			else
			{
				int o1l = o1.length(), o2l = o2.length();

				if (o1l > o2l)
					return 1;
				else if (o1l < o2l)
					return -1;
				else
					return o1.compareTo(o2);
			}
		}
	}
}
