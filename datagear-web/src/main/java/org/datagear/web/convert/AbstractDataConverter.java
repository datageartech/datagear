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

import org.datagear.model.support.DefaultDynamicBean;
import org.datagear.model.support.DynamicBean;
import org.datagear.model.support.PropertyPath;
import org.datagear.persistence.support.NameExpressionResolver;
import org.datagear.persistence.support.SqlExpressionResolver;
import org.datagear.persistence.support.VariableExpressionResolver;
import org.springframework.core.convert.ConversionService;

/**
 * 抽象数据转换器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataConverter
{
	/** 引用对象的引用属性名 */
	public static final String REF_NAME = "$ref";

	/** 引用根对象 */
	public static final String REF_VALUE_ROOT = "$";

	/** 引用上级对象 */
	public static final String REF_VALUE_PARENT = "..";

	/** 引用对象自身 */
	public static final String REF_VALUE_THIS = "@";

	/** 引用路径前缀 */
	public static final String REF_VALUE_PATH_PREFIX = "$";

	/** null值占位符，具有此值的源属性将被转换为null */
	public static final String NULL_VALUE_PLACE_HOLDER = "___DATA_GEAR_ZY_NULL_VALUE_PLACE_HOLDER___";

	protected static final PropertyPath REF_NAME_PROPERTY_PATH = PropertyPath.valueOf(REF_NAME);

	protected static final ArrayElementKeyComparator ARRAY_ELEMENT_KEY_COMPARATOR = new ArrayElementKeyComparator();

	private ConversionService conversionService;

	private NameExpressionResolver variableExpressionResolver = new VariableExpressionResolver();

	private NameExpressionResolver sqlExpressionResolver = new SqlExpressionResolver();

	private Map<Class<?>, Class<?>> instanceTypeMap = new HashMap<Class<?>, Class<?>>();

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

	public NameExpressionResolver getVariableExpressionResolver()
	{
		return variableExpressionResolver;
	}

	public void setVariableExpressionResolver(NameExpressionResolver variableExpressionResolver)
	{
		this.variableExpressionResolver = variableExpressionResolver;
	}

	public NameExpressionResolver getSqlExpressionResolver()
	{
		return sqlExpressionResolver;
	}

	public void setSqlExpressionResolver(NameExpressionResolver sqlExpressionResolver)
	{
		this.sqlExpressionResolver = sqlExpressionResolver;
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
		instanceTypeMap.put(DynamicBean.class, DefaultDynamicBean.class);

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
	 * 判断是否是引用映射表。
	 * 
	 * @param map
	 * @return
	 */
	protected boolean isRefMap(Map<?, ?> map)
	{
		if (map == null)
			return false;

		if (map.size() != 1)
			return false;

		if (map.containsKey(REF_NAME))
			return true;

		if (map.containsKey(REF_NAME_PROPERTY_PATH))
			return true;

		return false;
	}

	/**
	 * 获取映射表的引用值。
	 * 
	 * @param map
	 * @return
	 */
	protected String getRefValue(Map<?, ?> map)
	{
		Object rv = map.get(REF_NAME);

		if (rv == null)
			rv = map.get(REF_NAME_PROPERTY_PATH);

		return convertSimpleObj(rv, String.class, null);
	}

	/**
	 * 将{@linkplain #REF_NAME}的值解析为引用目标对象。
	 * 
	 * @param refContext
	 * @param ref
	 * @return
	 */
	protected Object resolveRefTarget(RefContext refContext, String ref)
	{
		if (REF_VALUE_ROOT.equals(ref))
			return refContext.getRoot();
		else if (REF_VALUE_PARENT.equals(ref))
			return refContext.getParent();
		else if (REF_VALUE_THIS.equals(ref))
			return refContext.getThis_();
		else if (ref.startsWith(REF_VALUE_PATH_PREFIX))
		{
			return refContext.getPathObject(ref);
		}
		else
			throw new IllegalArgumentException("Unknown [" + REF_NAME + "] value [" + ref + "]");
	}

	/**
	 * 将属性路径转换为引用路径。
	 * 
	 * @param propertyPath
	 * @return
	 */
	protected String toRefPath(String propertyPath)
	{
		if (propertyPath == null || propertyPath.isEmpty())
			return REF_VALUE_ROOT;
		else
			return REF_VALUE_PATH_PREFIX
					+ (propertyPath.startsWith(PropertyPath.ELEMENT_L + "") ? propertyPath : "." + propertyPath);
	}

	/**
	 * 连接属性路径。
	 * 
	 * @param parent
	 * @param child
	 */
	protected String concatPropertyPath(String parent, PropertyPath child)
	{
		return child.concatTo(parent);
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

	protected boolean isExpression(Object obj)
	{
		return this.variableExpressionResolver.isExpression(obj) || this.sqlExpressionResolver.isExpression(obj);
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

	/**
	 * 属性值映射表。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class PropValueMap implements Map<PropertyPath, Object>
	{
		private Map<PropertyPath, Object> map;

		public PropValueMap()
		{
			super();
			this.map = new HashMap<PropertyPath, Object>();
		}

		public PropValueMap(Map<PropertyPath, Object> map)
		{
			super();
			this.map = map;
		}

		public Map<PropertyPath, Object> getMap()
		{
			return map;
		}

		public void setMap(Map<PropertyPath, Object> map)
		{
			this.map = map;
		}

		@Override
		public int size()
		{
			return this.map.size();
		}

		@Override
		public boolean isEmpty()
		{
			return this.map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key)
		{
			return this.map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value)
		{
			return this.map.containsValue(value);
		}

		@Override
		public Object get(Object key)
		{
			return this.map.get(key);
		}

		@Override
		public Object put(PropertyPath key, Object value)
		{
			return this.map.put(key, value);
		}

		@Override
		public Object remove(Object key)
		{
			return this.map.remove(key);
		}

		@Override
		public void putAll(Map<? extends PropertyPath, ? extends Object> m)
		{
			this.map.putAll(m);
		}

		@Override
		public void clear()
		{
			this.map.clear();
		}

		@Override
		public Set<PropertyPath> keySet()
		{
			return this.map.keySet();
		}

		@Override
		public Collection<Object> values()
		{
			return this.map.values();
		}

		@Override
		public Set<java.util.Map.Entry<PropertyPath, Object>> entrySet()
		{
			return this.map.entrySet();
		}

		@Override
		public int hashCode()
		{
			return this.map.hashCode();
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
			PropValueMap other = (PropValueMap) obj;
			if (map == null)
			{
				if (other.map != null)
					return false;
			}
			else if (!map.equals(other.map))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return this.map.toString();
		}
	}

	/**
	 * 关联信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class RefContext
	{
		private Object root;

		private Object parent;

		private Object this_;

		private Map<String, Object> pathObjects = new HashMap<String, Object>();

		private Map<String, String> lazyRefs = new HashMap<String, String>();

		public RefContext()
		{
			super();
		}

		public Object getRoot()
		{
			return root;
		}

		public void setRoot(Object root)
		{
			this.root = root;
		}

		public Object getParent()
		{
			return parent;
		}

		public void setParent(Object parent)
		{
			this.parent = parent;
		}

		public Object getThis_()
		{
			return this_;
		}

		public void setThis_(Object this_)
		{
			this.this_ = this_;
		}

		public Object getPathObject(String path)
		{
			return this.pathObjects.get(path);
		}

		public void addPathObject(String path, Object object)
		{
			this.pathObjects.put(path, object);

			if (REF_VALUE_ROOT.equals(path))
				this.root = object;
		}

		public boolean hasLazyRefs()
		{
			return (this.lazyRefs != null && !this.lazyRefs.isEmpty());
		}

		public Map<String, String> getLazyRefs()
		{
			return lazyRefs;
		}

		public void addLazyRefs(String path, String refValue)
		{
			this.lazyRefs.put(path, refValue);
		}
	}
}
