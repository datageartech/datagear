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
import org.datagear.model.Property;
import org.datagear.model.features.NotReadable;
import org.datagear.model.support.DefaultDynamicBean;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.model.support.PropertyPath;
import org.datagear.persistence.collection.SizeOnlyCollection;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.RelationMapper;
import org.springframework.core.convert.ConversionService;

/**
 * {@linkplain Model}数据转换器。
 * 
 * @author datagear@163.com
 *
 */
public class ModelDataConverter extends AbstractDataConverter
{
	public ModelDataConverter()
	{
		super();
	}

	public ModelDataConverter(ConversionService conversionService)
	{
		super(conversionService);
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的数据。
	 * 
	 * @param obj
	 * @param model
	 * @return
	 * @throws ConverterException
	 */
	public Object convert(Object obj, Model model) throws ConverterException
	{
		return convertObj(null, obj, model, new RefContext());
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的数据数组。
	 * 
	 * @param obj
	 * @param model
	 * @return
	 * @throws ConverterException
	 */
	public Object[] convertToArray(Object obj, Model model) throws ConverterException
	{
		return convertObjToArray(null, obj, model, new RefContext());
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的集合。
	 * 
	 * @param obj
	 * @param model
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Object> convertToCollection(Object obj, Model model, Class<? extends Collection> collectionType)
			throws ConverterException
	{
		return convertObjToCollection(null, obj, model, collectionType, new RefContext());
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的数据。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Object convertObj(String namePath, Object obj, Model model, RefContext refContext)
			throws ConverterException
	{
		if (model.hasProperty())
		{
			if (obj == null)
				return null;
			else if (obj instanceof Map<?, ?>)
			{
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) obj;

				Object target = convertMap(namePath, map, model, refContext);

				if (target instanceof DefaultDynamicBean)
					((DefaultDynamicBean) target).setModel(model);

				return target;
			}
			else
				throw new ConverterException("Unsupported");
		}
		else
		{
			return convertSimpleObj(obj, model, namePath);
		}
	}

	/**
	 * 将映射表转换为指定{@linkplain Model}的数据。
	 * 
	 * @param namePath
	 * @param map
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Object convertMap(String namePath, Map<?, ?> map, Model model, RefContext refContext)
			throws ConverterException
	{
		if (map == null)
			return null;

		if (isRefMap(map))
		{
			String ref = getRefValue(map);
			return resolveRefTarget(ref, refContext);
		}

		Object re = model.newInstance();

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		if (map.isEmpty())
			return re;

		PropValueMap propValueMap = wrap(map);

		for (Map.Entry<PropertyPath, ?> entry : propValueMap.entrySet())
		{
			PropertyPath propertyPath = entry.getKey();
			Object rawValue = entry.getValue();

			// 忽略无关
			if (!propertyPath.isPropertyHead())
				continue;

			String propName = propertyPath.getPropertyNameHead();
			Property property = model.getProperty(propName);

			// 忽略无关
			if (property == null)
				continue;

			if (property.hasFeature(NotReadable.class))
				continue;

			PropertyModel propertyModel = getPropertyModel(property, propertyPath);
			Model pmodel = propertyModel.getModel();

			String myFullPropertyPath = concatPropertyPath(namePath, propertyPath);

			Object value = null;

			if (MU.isMultipleProperty(property))
			{
				if (property.isCollection())
					value = convertObjToCollection(myFullPropertyPath, rawValue, pmodel, property.getCollectionType(),
							refContext);
				else
					value = convertObjToArray(myFullPropertyPath, rawValue, pmodel, refContext);
			}
			else
			{
				value = convertObj(myFullPropertyPath, rawValue, pmodel, refContext);
			}

			setPropertyValue(pmodel, property, propertyModel, re, value);
		}

		return re;
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的数据数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("unchecked")
	protected Object[] convertObjToArray(String namePath, Object obj, Model model, RefContext refContext)
			throws ConverterException
	{
		if (obj == null)
			return null;

		Class<?> objClass = obj.getClass();

		if (Map.class.isAssignableFrom(objClass))
		{
			Map<String, ?> map = (Map<String, ?>) obj;

			Object[] array = convertMapToArray(namePath, map, model, refContext);

			return array;
		}
		else if (objClass.isArray())
		{
			return convertArrayToArray(namePath, (Object[]) obj, model, refContext);
		}
		else if (Collection.class.isAssignableFrom(objClass))
		{
			return convertCollectionToArray(namePath, (Collection<Object>) obj, model, refContext);
		}
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * 将对象转换为指定{@linkplain Model}的数据列表。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Collection<Object> convertObjToCollection(String namePath, Object obj, Model model,
			Class<? extends Collection> collectionType, RefContext refContext) throws ConverterException
	{
		if (obj == null)
			return null;

		Collection<Object> collection = null;

		Class<?> objClass = obj.getClass();

		if (Map.class.isAssignableFrom(objClass))
		{
			Map<String, ?> map = (Map<String, ?>) obj;

			if (SizeOnlyCollection.isSizeOnlyMapForStringMap(map))
				collection = convertMapToSizeOnlyCollection(namePath, map, collectionType);
			else
				collection = convertMapToCollection(namePath, map, model, collectionType, refContext);
		}
		else if (objClass.isArray())
		{
			collection = convertArrayToCollection(namePath, (Object[]) obj, model, collectionType, refContext);
		}
		else if (Collection.class.isAssignableFrom(objClass))
		{
			collection = convertCollectionToCollection(namePath, (Collection<Object>) obj, model, collectionType,
					refContext);
		}
		else
			throw new UnsupportedOperationException();

		return collection;
	}

	/**
	 * 将映射表转换为{@linkplain SizeOnlyCollection}。
	 * 
	 * @param namePath
	 * @param map
	 * @param collectionType
	 * @return
	 * @throws ConverterException
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Collection<?>> T convertMapToSizeOnlyCollection(String namePath, Map<?, ?> map,
			Class<? extends T> collectionType) throws ConverterException
	{
		int size = 0;

		Object value = SizeOnlyCollection.getSizeValue(map);

		if (value != null)
			size = convertSimpleObj(value, Integer.class, namePath);

		SizeOnlyCollection<Object> re = SizeOnlyCollection.instance(collectionType);
		re.setSize(size);

		return (T) re;
	}

	/**
	 * 将映射表转换为指定{@linkplain Model}的数据集合。
	 * 
	 * @param namePath
	 * @param map
	 *            原映射表，{@linkplain PropertyPath#isElementHead()}为{@code false}的关键字将被忽略
	 * @param model
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Collection<Object> convertMapToCollection(String namePath, Map<?, ?> map, Model model,
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
		Collection<Object> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (Map.Entry<PropertyPath, ?> entry : list)
		{
			Object src = entry.getValue();

			Object element = convertObj(concatPropertyPath(namePath, entry.getKey()), src, model, refContext);

			re.add(element);
		}

		return re;
	}

	/**
	 * 将数组转换为指定{@linkplain Model}的数据列表。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param collectionType
	 * @param refContext
	 * @return
	 */
	protected Collection<Object> convertArrayToCollection(String namePath, Object[] obj, Model model,
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType, RefContext refContext)
	{
		@SuppressWarnings("unchecked")
		Collection<Object> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (int i = 0; i < obj.length; i++)
		{
			Object element = obj[i];

			Object target = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element,
					model, refContext);

			re.add(target);
		}

		return re;
	}

	/**
	 * 将{@linkplain Collection}转换为指定{@linkplain Model}的数据列表。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param collectionType
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Collection<Object> convertCollectionToCollection(String namePath, Collection<?> obj, Model model,
			@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType, RefContext refContext)
			throws ConverterException
	{
		@SuppressWarnings("unchecked")
		Collection<Object> re = createInstance(collectionType);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int i = 0;
		for (Object element : obj)
		{
			Object target = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element,
					model, refContext);

			re.add(target);

			i++;
		}

		return re;
	}

	/**
	 * 将映射表转换为指定{@linkplain Model}的数据数组。
	 * 
	 * @param namePath
	 * @param map
	 *            原映射表，{@linkplain PropertyPath#isElementHead()}为{@code false}的关键字将被忽略
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Object[] convertMapToArray(String namePath, Map<?, ?> map, Model model, RefContext refContext)
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

		Object[] re = createArrayInstance(model.getType(), list.size());

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int index = 0;
		for (Map.Entry<PropertyPath, ?> entry : list)
		{
			Object src = entry.getValue();

			Object element = convertObj(concatPropertyPath(namePath, entry.getKey()), src, model, refContext);

			re[index++] = element;
		}

		return re;
	}

	/**
	 * 将数组转换为指定{@linkplain Model}的数据数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Object[] convertArrayToArray(String namePath, Object[] obj, Model model, RefContext refContext)
			throws ConverterException
	{
		Object[] re = createArrayInstance(model.getType(), obj.length);

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		for (int i = 0; i < re.length; i++)
			re[i] = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), obj[i], model,
					refContext);

		return re;
	}

	/**
	 * 将{@link Collection}转换为指定{@linkplain Model}的数据数组。
	 * 
	 * @param namePath
	 * @param obj
	 * @param model
	 * @param refContext
	 * @return
	 * @throws ConverterException
	 */
	protected Object[] convertCollectionToArray(String namePath, Collection<?> obj, Model model, RefContext refContext)
			throws ConverterException
	{
		Object[] re = createArrayInstance(model.getType(), obj.size());

		refContext.addPathObject(toRefPath(namePath), re);
		refContext.setParent(refContext.getThis_());
		refContext.setThis_(re);

		int i = 0;
		for (Object element : obj)
		{
			re[i] = convertObj(concatPropertyPath(namePath, PropertyPath.valueOf("[" + i + "]")), element, model,
					refContext);
			i++;
		}

		return re;
	}

	/**
	 * 将简单对象转换为指定{@linkplain Model}的对象。
	 * 
	 * @param obj
	 * @param model
	 * @param propertyPath
	 *            允许为{@code null}
	 * @return
	 * @throws IllegalSourceValueException
	 */
	protected Object convertSimpleObj(Object obj, Model model, String propertyPath) throws IllegalSourceValueException
	{
		return convertSimpleObj(obj, model.getType(), propertyPath);
	}

	/**
	 * 获取属性模型。
	 * <p>
	 * 如果无法获取到，将返回{@code null}。
	 * </p>
	 * 
	 * @param property
	 * @param propertyPath
	 * @return
	 */
	protected PropertyModel getPropertyModel(Property property, PropertyPath propertyPath)
	{
		if (!property.isAbstracted())
			return PropertyModel.valueOf(property, 0);
		else
		{
			if (propertyPath.hasPropertyModelIndexHead())
				return PropertyModel.valueOf(property, propertyPath.getPropertyModelIndexHead());
			else
				throw new IllegalArgumentException("The property model is not explicit");
		}
	}

	/**
	 * 设置属性值。
	 * 
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @param obj
	 * @param propertyValue
	 */
	protected void setPropertyValue(Model model, Property property, PropertyModel propertyModel, Object obj,
			Object propertyValue)
	{
		Model pmodel = propertyModel.getModel();

		if (propertyValue == null)
		{
			if (pmodel.getType().isPrimitive())
				;
			else
				property.set(obj, null);
		}
		else if (MU.isPrimitiveModel(pmodel))
		{
			property.set(obj, propertyValue);
		}
		else
		{
			property.set(obj, propertyValue);

			RelationMapper relationMapper = property.getFeature(RelationMapper.class);
			Mapper mapper = relationMapper.getMappers()[propertyModel.getIndex()];

			String mappedTarget = null;
			if (mapper.isMappedBySource())
				mappedTarget = mapper.getMappedByTarget();
			else if (mapper.isMappedByTarget())
				mappedTarget = mapper.getMappedBySource();

			// 反向设置属性值
			if (mappedTarget != null)
			{
				Property mappedProperty = MU.getProperty(pmodel, mappedTarget);

				if (property.isCollection())
				{
					@SuppressWarnings("unchecked")
					Collection<Object> collection = (Collection<Object>) propertyValue;

					for (Object ele : collection)
						mappedProperty.set(ele, obj);
				}
				else if (property.isArray())
				{
					Object[] array = (Object[]) propertyValue;

					for (Object ele : array)
						mappedProperty.set(ele, obj);
				}
				else
					mappedProperty.set(propertyValue, obj);
			}
		}
	}
}
