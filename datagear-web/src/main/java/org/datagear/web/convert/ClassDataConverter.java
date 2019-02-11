/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.model.Model;
import org.datagear.model.support.PropertyPath;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

/**
 * {@linkplain Class}数据转换器。
 * 
 * @author datagear@163.com
 *
 */
public class ClassDataConverter extends AbstractDataConverter
{
	public ClassDataConverter()
	{
		super();
	}

	public ClassDataConverter(ConversionService conversionService)
	{
		super(conversionService);
	}

	/**
	 * 将对象转换为指定类型的数据。
	 * 
	 * @param obj
	 * @param type
	 * @return
	 * @throws ConverterException
	 */
	public <T> T convert(Object obj, Class<T> type) throws ConverterException
	{
		RefContext refContext = new RefContext();

		T target = convertObj(null, obj, type, refContext);

		handleLazyRefs(target, refContext);

		return target;
	}

	/**
	 * 将对象转换为指定类型的对象数组。
	 * 
	 * @param obj
	 * @param type
	 * @return
	 * @throws ConverterException
	 */
	public <T> T[] convertToArray(Object obj, Class<T> type) throws ConverterException
	{
		RefContext refContext = new RefContext();

		T[] target = convertObjToArray(null, obj, type, refContext);

		handleLazyRefs(target, refContext);

		return target;
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的集合。
	 * 
	 * @param obj
	 * @param type
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("rawtypes")
	public <T> Collection<T> convertToCollection(Object obj, Class<T> type, Class<? extends Collection> collectionType)
			throws ConverterException
	{
		RefContext refContext = new RefContext();

		Collection<T> target = convertObjToCollection(null, obj, type, collectionType, refContext);

		handleLazyRefs(target, refContext);

		return target;
	}

	/**
	 * 将对象转换为指定类型的对象。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("unchecked")
	protected <T> T convertObj(String namePath, Object obj, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		if (obj == null)
		{
			if (type.isPrimitive())
				throw new IllegalSourceValueException(namePath, obj, type);
			else
				return null;
		}
		else if (obj instanceof Map<?, ?>)
		{
			Map<String, Object> map = (Map<String, Object>) obj;

			T target = null;

			if (type.isArray())
				target = (T) convertMapToArray(namePath, map, type.getComponentType(), refContext);
			else
				target = convertMap(namePath, map, type, refContext);

			return target;
		}
		else
		{
			return convertSimpleObj(obj, type, namePath);
		}
	}

	/**
	 * 将映射表转换为指定类型的对象。
	 * 
	 * @param namePath
	 * @param map
	 * @param type
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("unchecked")
	protected <T> T convertMap(String namePath, Map<?, ?> map, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		if (map == null)
			return null;

		if (type.isInstance(map))
			return (T) map;

		if (isRefMap(map))
		{
			String refValue = getRefValue(map);
			Object refTarget = resolveRefTarget(refContext, refValue);

			// 引用有可能在目标未被解析前先被处理，此时需要延迟处理
			if (refTarget != null)
				return (T) refTarget;
			else
			{
				refContext.addLazyRefs(namePath, refValue);

				return null;
			}
		}

		T re = createInstance(type);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		if (map.isEmpty())
			return re;

		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(re);

		PropValueMap propValueMap = wrap(map);

		for (Map.Entry<PropertyPath, ?> entry : propValueMap.entrySet())
		{
			PropertyPath propertyPath = entry.getKey();
			Object rawValue = entry.getValue();

			// 忽略无关
			if (!propertyPath.isPropertyHead())
				continue;

			String propName = propertyPath.getPropertyNameHead();
			TypeDescriptor propertyTypeDescriptor = getPropertyTypeDescriptor(beanWrapper, propName);

			// 忽略无关
			if (propertyTypeDescriptor == null)
				continue;

			String myFullPropertyPath = concatPropertyPath(namePath, propertyPath);

			if (propertyTypeDescriptor.isArray())
			{
				Class<?> elementType = propertyTypeDescriptor.getElementTypeDescriptor().getType();

				Object value = convertObjToArray(myFullPropertyPath, rawValue, elementType, refContext);

				beanWrapper.setPropertyValue(propName, value);
			}
			else if (propertyTypeDescriptor.isCollection())
			{
				Class<?> elementType = propertyTypeDescriptor.getElementTypeDescriptor().getType();

				@SuppressWarnings("rawtypes")
				Object value = convertObjToCollection(myFullPropertyPath, rawValue, elementType,
						(Class<? extends Collection>) propertyTypeDescriptor.getType(), refContext);

				beanWrapper.setPropertyValue(propName, value);
			}
			else
			{
				Class<?> propertyType = propertyTypeDescriptor.getType();
				Object value = convertObj(myFullPropertyPath, rawValue, propertyType, refContext);

				if (value == null)
				{
					if (propertyType.isPrimitive())
						;
					else
						beanWrapper.setPropertyValue(propName, null);
				}
				else
					beanWrapper.setPropertyValue(propName, value);
			}
		}

		return re;
	}

	/**
	 * 将对象转换为指定类型的集合。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> Collection<T> convertObjToCollection(String namePath, Object obj, Class<T> type,
			Class<? extends Collection> collectionType, RefContext refContext) throws ConverterException
	{
		if (obj == null)
			return null;

		Collection<T> collection = null;

		Class<?> objClass = obj.getClass();

		if (Map.class.isAssignableFrom(objClass))
		{
			Map<String, ?> map = (Map<String, ?>) obj;

			collection = convertMapToCollection(namePath, map, type, collectionType, refContext);
		}
		else if (objClass.isArray())
		{
			collection = convertArrayToCollection(namePath, (Object[]) obj, type, collectionType, refContext);
		}
		else if (Collection.class.isAssignableFrom(objClass))
		{
			collection = convertCollectionToCollection(namePath, (Collection<Object>) obj, type, collectionType,
					refContext);
		}
		else
			collection = (Collection<T>) convertSimpleObj(obj, type, namePath);

		return collection;
	}

	/**
	 * 将映射表转换为指定类型的集合。
	 * 
	 * @param namePath
	 * @param map
	 *            原映射表，{@linkplain PropertyPath#isElementHead()}为{@code false}的关键字将被忽略
	 * @param type
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected <T> Collection<T> convertMapToCollection(String namePath, Map<?, ?> map, Class<T> type,
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType, RefContext refContext)
			throws ConverterException
	{
		PropValueMap propValueMap = wrap(map);

		List<Map.Entry<PropertyPath, ?>> list = new ArrayList<Map.Entry<PropertyPath, ?>>();

		for (Map.Entry<PropertyPath, ?> entry : propValueMap.entrySet())
		{
			if (!entry.getKey().isElementHead())
				continue;

			list.add(entry);
		}

		Collections.sort(list, ARRAY_ELEMENT_KEY_COMPARATOR);

		@SuppressWarnings("unchecked")
		Collection<T> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (Map.Entry<PropertyPath, ?> entry : list)
		{
			Object src = entry.getValue();

			T element = convertObj(concatPropertyPath(namePath, entry.getKey()), src, type, refContext);

			re.add(element);
		}

		return re;
	}

	/**
	 * 将数组转换为指定类型的集合。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param collectionType
	 * @param refContext
	 * @return
	 */
	protected <T> Collection<T> convertArrayToCollection(String namePath, Object[] obj, Class<T> type,
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType, RefContext refContext)
	{
		@SuppressWarnings("unchecked")
		Collection<T> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (int i = 0; i < obj.length; i++)
		{
			Object element = obj[i];

			T target = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element, type,
					refContext);

			re.add(target);
		}

		return re;
	}

	/**
	 * 将{@linkplain Collection}转换为指定类型的列表。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected <T> Collection<T> convertCollectionToCollection(String namePath, Collection<?> obj, Class<T> type,
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType, RefContext refContext)
			throws ConverterException
	{
		@SuppressWarnings("unchecked")
		Collection<T> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int i = 0;
		for (Object element : obj)
		{
			T target = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element, type,
					refContext);

			re.add(target);

			i++;
		}

		return re;
	}

	/**
	 * 将对象转换为指定类型的数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("unchecked")
	protected <T> T[] convertObjToArray(String namePath, Object obj, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		if (obj == null)
			return null;

		Class<?> objClass = obj.getClass();

		if (Map.class.isAssignableFrom(objClass))
		{
			Map<String, ?> map = (Map<String, ?>) obj;

			T[] array = convertMapToArray(namePath, map, type, refContext);
			return array;
		}
		else if (objClass.isArray())
		{
			return convertArrayToArray(namePath, (Object[]) obj, type, refContext);
		}
		else if (Collection.class.isAssignableFrom(objClass))
		{
			return convertCollectionToArray(namePath, (Collection<Object>) obj, type, refContext);
		}
		else
			return (T[]) convertSimpleObj(obj, type, namePath);
	}

	/**
	 * 将映射表转换为指定类型的数组。
	 * 
	 * @param namePath
	 * @param map
	 *            原映射表，{@linkplain PropertyPath#isElementHead()}为{@code false}的关键字将被忽略
	 * @param type
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected <T> T[] convertMapToArray(String namePath, Map<?, ?> map, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		PropValueMap propValueMap = wrap(map);

		List<Map.Entry<PropertyPath, ?>> list = new ArrayList<Map.Entry<PropertyPath, ?>>();

		for (Map.Entry<PropertyPath, ?> entry : propValueMap.entrySet())
		{
			if (!entry.getKey().isElementHead())
				continue;

			list.add(entry);
		}

		Collections.sort(list, ARRAY_ELEMENT_KEY_COMPARATOR);

		T[] re = createArrayInstance(type, list.size());

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int index = 0;
		for (Map.Entry<PropertyPath, ?> entry : list)
		{
			Object src = entry.getValue();

			T element = convertObj(concatPropertyPath(namePath, entry.getKey()), src, type, refContext);

			re[index++] = element;
		}

		return re;
	}

	/**
	 * 将数组转换为指定类型的数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected <T> T[] convertArrayToArray(String namePath, Object[] obj, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		T[] re = createArrayInstance(type, obj.length);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (int i = 0; i < re.length; i++)
			re[i] = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), obj[i], type,
					refContext);

		return re;
	}

	/**
	 * 将{@link Collection}转换为指定类型的数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param type
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected <T> T[] convertCollectionToArray(String namePath, Collection<?> obj, Class<T> type, RefContext refContext)
			throws ConverterException
	{
		T[] re = createArrayInstance(type, obj.size());

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int i = 0;
		for (Object element : obj)
		{
			re[i] = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element, type,
					refContext);
			i++;
		}

		return re;
	}

	/**
	 * 处理延迟引用。
	 * 
	 * @param obj
	 * @param refContext
	 */
	protected void handleLazyRefs(Object obj, RefContext refContext)
	{
		if (obj == null)
			return;

		if (!refContext.hasLazyRefs())
			return;

		Map<String, String> lazyRefs = refContext.getLazyRefs();

		for (Map.Entry<String, String> entry : lazyRefs.entrySet())
		{
			String refValue = entry.getValue();

			Object target = resolveRefTarget(refContext, refValue);

			if (target != null)
			{
				PropertyPath propertyPath = PropertyPath.valueOf(entry.getKey());

				setValue(obj, propertyPath, target);
			}
		}
	}

	/**
	 * 设置对象属性值。
	 * 
	 * @param obj
	 * @param propertyPath
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	protected void setValue(Object obj, PropertyPath propertyPath, Object value)
	{
		Object owner = obj;

		for (int i = 0, len = propertyPath.length(); i < len; i++)
		{
			if (owner == null)
				throw new ConverterException("The owner object of index [" + i + "] must not be null");

			if (i == len - 1)
			{
				if (owner instanceof Map<?, ?>)
				{
					if (!propertyPath.isProperty(i))
						throw new ConverterException("The " + i + "-th must be property of [" + propertyPath + "]");

					((Map<String, Object>) owner).put(propertyPath.getPropertyName(i), value);
				}
				else if (owner instanceof Object[])
				{
					if (!propertyPath.isElement(i))
						throw new ConverterException("The " + i + "-th must be element of [" + propertyPath + "]");

					((Object[]) owner)[propertyPath.getElementIndex(i)] = value;
				}
				else if (owner instanceof List<?>)
				{
					if (!propertyPath.isElement(i))
						throw new ConverterException("The " + i + "-th must be element of [" + propertyPath + "]");

					((List<Object>) owner).set(propertyPath.getElementIndex(i), value);
				}
				else
				{
					if (!propertyPath.isProperty(i))
						throw new ConverterException("The " + i + "-th must be property of [" + propertyPath + "]");

					BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(owner);
					beanWrapper.setPropertyValue(propertyPath.getPropertyName(i), value);
				}
			}
			else
			{
				if (owner instanceof Map<?, ?>)
				{
					if (!propertyPath.isProperty(i))
						throw new ConverterException("The " + i + "-th must be property of [" + propertyPath + "]");

					owner = ((Map<String, Object>) owner).get(propertyPath.getPropertyName(i));
				}
				else if (owner instanceof Object[])
				{
					if (!propertyPath.isElement(i))
						throw new ConverterException("The " + i + "-th must be element of [" + propertyPath + "]");

					owner = ((Object[]) owner)[propertyPath.getElementIndex(i)];
				}
				else if (owner instanceof List<?>)
				{
					if (!propertyPath.isElement(i))
						throw new ConverterException("The " + i + "-th must be element of [" + propertyPath + "]");

					owner = ((List<Object>) owner).get(propertyPath.getElementIndex(i));
				}
				else
				{
					if (!propertyPath.isProperty(i))
						throw new ConverterException("The " + i + "-th must be property of [" + propertyPath + "]");

					BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(owner);
					owner = beanWrapper.getPropertyValue(propertyPath.getPropertyName(i));
				}
			}
		}
	}

	/**
	 * 获取属性类型信息。
	 * <p>
	 * 如果没有此属性，将返回{@code null}。
	 * </p>
	 * 
	 * @param beanWrapper
	 * @param propName
	 * @return
	 */
	protected TypeDescriptor getPropertyTypeDescriptor(BeanWrapper beanWrapper, String propName)
	{
		try
		{
			return beanWrapper.getPropertyTypeDescriptor(propName);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
