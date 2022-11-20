/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginDataSetRange.Range;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Group;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * JSON {@linkplain ChartPlugin}属性解析器。
 * <p>
 * 此类从JSON解析{@linkplain ChartPlugin}对象的属性：
 * </p>
 * <code>
 * <pre>
 * {
 *   id : "...",
 *   nameLabel : "..." 或者 { value : "...", localeValues : { "zh" : "...", "en" : "..." }},
 *   descLabel : "..." 或者 { ... },
 *   icons : "..." 或者 { "LIGHT" : "icons/light.png", "DARK" : "icons/dark.png" },
 *   attributes :  [ { ... }, ... ],
 *   dataSigns : [ { ... }, ... ],
 *   dataSetRange: { ... },
 *   version : "...",
 *   order: 整数值,
 *   categories: "..." 或者 {name: "...", ...} 或者 ["...", "...", ...] 或者 [ {name: "...", ...}, {name: "...", ...}, ... ],
 *   或者（兼容3.0.1版本格式）
 *   category: "..." 或者 {name: "...", ...} 或者 ["...", "...", ...] 或者 [ {name: "...", ...}, {name: "...", ...}, ... ],
 *   categoryOrders: 整数值 或者 [ 整数值, 整数值, ... ]
 * }
 * </pre>
 * </code>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginPropertiesResolver
{
	public static final String JSON_PROPERTY_ID = ChartPlugin.PROPERTY_ID;
	public static final String JSON_PROPERTY_NAME_LABEL = ChartPlugin.PROPERTY_NAME_LABEL;
	public static final String JSON_PROPERTY_DESC_LABEL = ChartPlugin.PROPERTY_DESC_LABEL;
	public static final String JSON_PROPERTY_ATTRIBUTES = ChartPlugin.PROPERTY_ATTRIBUTES;
	public static final String JSON_PROPERTY_DATA_SIGNS = ChartPlugin.PROPERTY_DATA_SIGNS;
	public static final String JSON_PROPERTY_DATA_SET_RANGE = ChartPlugin.PROPERTY_DATA_SET_RANGE;
	public static final String JSON_PROPERTY_VERSION = ChartPlugin.PROPERTY_VERSION;
	public static final String JSON_PROPERTY_ORDER = ChartPlugin.PROPERTY_ORDER;
	public static final String JSON_PROPERTY_CATEGORIES = ChartPlugin.PROPERTY_CATEGORIES;
	public static final String JSON_PROPERTY_CATEGORY_ORDERS = ChartPlugin.PROPERTY_CATEGORY_ORDERS;
	public static final String JSON_PROPERTY_ICONS = "icons";

	/**
	 * 3.0.1版本的单类别属性名，已在3.1.0版本中被{@linkplain #JSON_PROPERTY_CATEGORIES}代替。
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static final String JSON_PROPERTY_CATEGORY_3_0_1 = "category";

	public JsonChartPluginPropertiesResolver()
	{
		super();
	}

	/**
	 * 从映射表解析并设置{@linkplain ChartPlugin}属性。
	 * <p>
	 * 它会进行必要的类型转换。
	 * </p>
	 * 
	 * @param chartPlugin
	 * @param properties
	 */
	public void resolveChartPluginProperties(AbstractChartPlugin chartPlugin, Map<String, ?> properties)
	{
		chartPlugin.setId((String) properties.get(JSON_PROPERTY_ID));
		chartPlugin.setNameLabel(convertToLabel(properties.get(JSON_PROPERTY_NAME_LABEL)));
		chartPlugin.setDescLabel(convertToLabel(properties.get(JSON_PROPERTY_DESC_LABEL)));
		chartPlugin.setIconResourceNames(convertToIconResourceNames(properties.get(JSON_PROPERTY_ICONS)));
		chartPlugin.setAttributes(convertToAttributes(properties.get(JSON_PROPERTY_ATTRIBUTES)));
		chartPlugin.setDataSigns(convertToDataSigns(properties.get(JSON_PROPERTY_DATA_SIGNS)));
		chartPlugin.setDataSetRange(convertToDataSetRange(properties.get(JSON_PROPERTY_DATA_SET_RANGE)));
		chartPlugin.setVersion((String) properties.get(JSON_PROPERTY_VERSION));
		chartPlugin.setOrder(convertToInt(properties.get(JSON_PROPERTY_ORDER), chartPlugin.getOrder()));

		Object categoriesObj = properties.get(JSON_PROPERTY_CATEGORIES);
		if (categoriesObj == null)
			categoriesObj = properties.get(JSON_PROPERTY_CATEGORY_3_0_1);
		chartPlugin.setCategories(convertToCategories(categoriesObj));

		chartPlugin.setCategoryOrders(
				convertToCategoryOrders(properties.get(JSON_PROPERTY_CATEGORY_ORDERS), chartPlugin.getOrder()));
	}

	/**
	 * 从JSON字符串解析并设置{@linkplain ChartPlugin}属性。
	 * 
	 * @param chartPlugin
	 * @param json
	 * @throws IOException
	 */
	public void resolveChartPluginProperties(AbstractChartPlugin chartPlugin, String json) throws IOException
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = JsonSupport.parseNonStardand(json, Map.class);
		resolveChartPluginProperties(chartPlugin, properties);
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain ChartPlugin}属性。
	 * 
	 * @param chartPlugin
	 * @param jsonReader
	 * @throws IOException
	 */
	public void resolveChartPluginProperties(AbstractChartPlugin chartPlugin, Reader jsonReader) throws IOException
	{
		String json = null;

		StringWriter writer = null;
		try
		{
			writer = new StringWriter();
			IOUtil.write(jsonReader, writer);
		}
		finally
		{
			IOUtil.close(writer);
		}

		json = writer.toString();

		resolveChartPluginProperties(chartPlugin, json);
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain ChartPlugin}属性。
	 * 
	 * @param chartPlugin
	 * @param in
	 * @param encoding
	 * @throws IOException
	 */
	public void resolveChartPluginProperties(AbstractChartPlugin chartPlugin, InputStream in, String encoding)
			throws IOException
	{
		Reader reader = IOUtil.getReader(in, encoding);
		resolveChartPluginProperties(chartPlugin, reader);
	}

	/**
	 * 将对象转换为图标资源名映射表。
	 * <p>
	 * 支持格式如下：
	 * </p>
	 * <p>
	 * {@code "..."}
	 * </p>
	 * <p>
	 * <code>{ "...": "...", ... }</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected Map<String, String> convertToIconResourceNames(Object obj)
	{
		if (obj == null)
		{
			return Collections.emptyMap();
		}
		else if (obj instanceof String)
		{
			Map<String, String> icons = new HashMap<>();
			icons.put(ChartPlugin.DEFAULT_ICON_THEME_NAME, (String) obj);

			return icons;
		}
		else if (obj instanceof Map<?, ?>)
		{
			Map<String, String> icons = new HashMap<>();

			Map<?, ?> map = (Map<?, ?>) obj;

			for (Map.Entry<?, ?> entry : map.entrySet())
				icons.put(entry.getKey().toString(), entry.getValue().toString());

			return icons;
		}
		else
			throw new UnsupportedOperationException(
					"Convert object of type [" + obj.getClass().getName() + "] to icon map is not supported");
	}

	/**
	 * 将对象转换为{@linkplain DataSign}列表。
	 * <p>
	 * 支持格式如下：
	 * </p>
	 * <p>
	 * <code>{ ... }</code>
	 * </p>
	 * <p>
	 * <code>[ { ... }, ... ]</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected List<DataSign> convertToDataSigns(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;
	
			List<DataSign> dataSigns = new ArrayList<>();
	
			for (Object ele : array)
			{
				DataSign dataSign = convertToDataSign(ele);
	
				if (dataSign != null)
					dataSigns.add(dataSign);
			}
	
			if (dataSigns.isEmpty())
				return null;
	
			return dataSigns;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);
	
			return convertToDataSigns(array);
		}
		else
		{
			Object[] array = new Object[] { obj };
	
			return convertToDataSigns(array);
		}
	}

	/**
	 * 将对象转换为{@linkplain DataSign}。
	 * 
	 * @param obj
	 * @return
	 */
	protected DataSign convertToDataSign(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof DataSign)
			return (DataSign) obj;
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;
	
			String name = (String) map.get(DataSign.PROPERTY_NAME);
			if (name == null || name.isEmpty())
				return null;
	
			DataSign dataSign = createDataSign();
			dataSign.setName(name);
	
			dataSign.setRequired(convertToDataSignRequired(map.get(DataSign.PROPERTY_REQUIRED)));
			dataSign.setMultiple(convertToDataSignMultiple(map.get(DataSign.PROPERTY_MULTIPLE)));
			dataSign.setNameLabel(convertToLabel(map.get(DataSign.PROPERTY_NAME_LABEL)));
			dataSign.setDescLabel(convertToLabel(map.get(DataSign.PROPERTY_DESC_LABEL)));
	
			return dataSign;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ DataSign.class.getName() + "] is not supported");
	}

	protected boolean convertToDataSignRequired(Object v)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		boolean dftValue = true;
		return convertToBoolean(v, dftValue);
	}

	protected boolean convertToDataSignMultiple(Object v)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		boolean dftValue = false;
		return convertToBoolean(v, dftValue);
	}

	/**
	 * 将对象转换为{@linkplain ChartPluginAttribute}列表。
	 * <p>
	 * 支持格式如下：
	 * </p>
	 * <p>
	 * <code>{ ... }</code>
	 * </p>
	 * <p>
	 * <code>[ { ... }, ... ]</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected List<ChartPluginAttribute> convertToAttributes(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<ChartPluginAttribute> attributes = new ArrayList<>();

			for (Object ele : array)
			{
				ChartPluginAttribute attribute = convertToAttribute(ele);

				if (attribute != null)
					attributes.add(attribute);
			}

			if (attributes.isEmpty())
				return null;

			return attributes;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);

			return convertToAttributes(array);
		}
		else
		{
			Object[] array = new Object[] { obj };

			return convertToAttributes(array);
		}
	}

	/**
	 * 将对象转换为{@linkplain ChartPluginAttribute}。
	 * 
	 * @param obj
	 * @return
	 */
	protected ChartPluginAttribute convertToAttribute(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof ChartPluginAttribute)
			return (ChartPluginAttribute) obj;
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			String name = (String) map.get(ChartPluginAttribute.PROPERTY_NAME);
			if (name == null || name.isEmpty())
				return null;

			ChartPluginAttribute attribute = createChartPluginAttribute();
			attribute.setName(name);
			attribute.setType(convertToAttributeType(map.get(ChartPluginAttribute.PROPERTY_TYPE)));
			attribute.setNameLabel(convertToLabel(map.get(ChartPluginAttribute.PROPERTY_NAME_LABEL)));
			attribute.setDescLabel(convertToLabel(map.get(ChartPluginAttribute.PROPERTY_DESC_LABEL)));
			attribute.setRequired(convertToAttributeRequired(map.get(ChartPluginAttribute.PROPERTY_REQUIRED)));
			attribute.setInputType(convertToAttributeInputType(map.get(ChartPluginAttribute.PROPERTY_INPUT_TYPE)));
			attribute.setInputPayload(convertToAttributeInputPayload(map.get(ChartPluginAttribute.PROPERTY_INPUT_PAYLOAD)));
			attribute.setGroup(convertToGroup(map.get(ChartPluginAttribute.PROPERTY_GROUP)));
			attribute.setAdditions(convertToAttributeAdditions(map.get(ChartPluginAttribute.PROPERTY_ADDITIONS)));

			return attribute;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginAttribute.class.getName() + "] is not supported");
	}

	protected boolean convertToAttributeRequired(Object v)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		boolean dftValue = false;
		return convertToBoolean(v, dftValue);
	}

	protected String convertToAttributeType(Object obj)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		String dftValue = ChartPluginAttribute.DataType.STRING;

		if (obj instanceof String)
		{
			String str = (String) obj;
			
			if (StringUtil.isEmpty(str))
			{
				return dftValue;
			}
			if(ChartPluginAttribute.DataType.STRING.equalsIgnoreCase(str))
			{
				return ChartPluginAttribute.DataType.STRING;
			}
			else if(ChartPluginAttribute.DataType.BOOLEAN.equalsIgnoreCase(str))
			{
				return ChartPluginAttribute.DataType.BOOLEAN;
			}
			else if(ChartPluginAttribute.DataType.NUMBER.equalsIgnoreCase(str))
			{
				return ChartPluginAttribute.DataType.NUMBER;
			}
			else
				return str;
		}
		else
			return dftValue;
	}

	protected String convertToAttributeInputType(Object obj)
	{
		if(obj == null)
			return  null;
		else if (obj instanceof String)
			return (String) obj;
		else
			return ChartPluginAttribute.InputType.TEXT;
	}

	protected Object convertToAttributeInputPayload(Object obj)
	{
		return obj;
	}

	protected Group convertToGroup(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Group)
			return (Group) obj;
		else if (obj instanceof String)
		{
			Group group = createGroup();
			group.setName((String) obj);
			return group;
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			String name = (String) map.get(Group.PROPERTY_NAME);
			if (name == null)
				return null;

			Group group = createGroup();
			group.setName(name);

			group.setNameLabel(convertToLabel(map.get(Group.PROPERTY_NAME_LABEL)));
			group.setDescLabel(convertToLabel(map.get(Group.PROPERTY_DESC_LABEL)));
			group.setOrder(convertToInt(map.get(Group.PROPERTY_ORDER), group.getOrder()));

			return group;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Group.class.getName() + "] is not supported");
	}

	@SuppressWarnings("unchecked")
	protected Map<String, ?> convertToAttributeAdditions(Object obj)
	{
		if(obj == null)
			return  null;
		else if (obj instanceof Map<?, ?>)
			return (Map<String, ?>) obj;
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginAttribute.class.getName() + ".additions] is not supported");
	}

	/**
	 * 将对象转换为{@linkplain ChartPluginDataSetRange}。
	 * <p>
	 * 支持如下三种格式：
	 * </p>
	 * <p>
	 * 仅定义{@linkplain ChartPluginDataSetRange#getMain()}的{@linkplain Range#getMin()}格式：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * 数值
	 * </pre>
	 * </code>
	 * </p>
	 * <p>
	 * 仅定义{@linkplain ChartPluginDataSetRange#getMain()}的格式：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * {
	 *   //可选
	 *   min: 数值,
	 *   //可选
	 *   max: 数值
	 * }
	 * </pre>
	 * </code>
	 * </p>
	 * <p>
	 * 完整格式：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * {
	 *   //可选
	 *   main:
	 *   {
	 *     //可选
	 *     min: 数值,
	 *     //可选
	 *     max: 数值
	 *   },
	 *   //可选
	 *   attachment:
	 *   {
	 *     //可选
	 *     min: 数值,
	 *     //可选
	 *     max: 数值
	 *   }
	 * }
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param obj
	 * @return 可能为{@code null}
	 */
	@SuppressWarnings("unchecked")
	protected ChartPluginDataSetRange convertToDataSetRange(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if(obj instanceof Number)
		{
			Range mainRange = createRange();
			mainRange.setMin(((Number) obj).intValue());
			
			ChartPluginDataSetRange dsr = createChartPluginDataSetRange();
			dsr.setMain(mainRange);
			
			return dsr;
		}
		else if (obj instanceof Map<?, ?>)
		{
			ChartPluginDataSetRange dsr = createChartPluginDataSetRange();

			Map<String, ?> map = (Map<String, ?>) obj;
			Map<String, ?> mainMap = (Map<String, ?>) map.get(ChartPluginDataSetRange.PROPERTY_MAIN);
			Map<String, ?> attachmentMap = (Map<String, ?>) map.get(ChartPluginDataSetRange.PROPERTY_ATTACHMENT);

			if (mainMap == null && attachmentMap == null)
			{
				Range main = convertToRange(map);

				if (main != null)
					dsr.setMain(main);
			}
			else
			{
				dsr.setMain(convertToRange(mainMap));
				dsr.setAttachment(convertToRange(attachmentMap));
			}

			return dsr;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginDataSetRange.class.getName() + "] is not supported");
	}

	protected Range convertToRange(Map<String, ?> map)
	{
		if (map == null || map.isEmpty())
			return null;
		
		Number min = (Number) map.get(ChartPluginDataSetRange.Range.PROPERTY_MIN);
		Number max = (Number) map.get(ChartPluginDataSetRange.Range.PROPERTY_MAX);
		
		if (min == null && max == null)
			return null;
		
		Range range = createRange();
		range.setMin(min == null ? null : min.intValue());
		range.setMax(max == null ? null : max.intValue());
		
		return range;
	}

	/**
	 * 将对象转换为{@linkplain Category}列表。
	 * <p>
	 * 支持格式如下：
	 * </p>
	 * <p>
	 * <code>"..."</code>
	 * </p>
	 * <p>
	 * <code>{ ... }</code>
	 * </p>
	 * <p>
	 * <code>[ { ... }, ..., "...", ... ]</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected List<Category> convertToCategories(Object obj)
	{
		List<Category> categories = new ArrayList<Category>(1);
		convertToCategories(categories, obj);

		return categories;
	}

	protected void convertToCategories(List<Category> categories, Object obj)
	{
		if (obj == null)
			return;
		else if (obj instanceof Category)
			categories.add((Category) obj);
		else if (obj instanceof String)
		{
			Category category = createCategory();
			category.setName((String) obj);
			categories.add(category);
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			String name = (String) map.get(Category.PROPERTY_NAME);
			if (name == null)
				return;

			Category category = createCategory();
			category.setName(name);

			category.setNameLabel(convertToLabel(map.get(Category.PROPERTY_NAME_LABEL)));
			category.setDescLabel(convertToLabel(map.get(Category.PROPERTY_DESC_LABEL)));
			category.setOrder(convertToInt(map.get(Category.PROPERTY_ORDER), category.getOrder()));

			categories.add(category);
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			for (Object ele : collection)
				convertToCategories(categories, ele);
		}
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;
			for (Object ele : array)
				convertToCategories(categories, ele);
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Category.class.getName() + "] is not supported");
	}

	protected List<Integer> convertToCategoryOrders(Object obj, int defaultOrder)
	{
		List<Integer> orders = new ArrayList<Integer>(1);

		if (obj != null)
			convertToCategoryOrders(orders, obj, defaultOrder);

		return orders;
	}

	protected void convertToCategoryOrders(List<Integer> orders, Object obj, int defaultOrder)
	{
		if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			for (Object ele : collection)
				convertToCategoryOrders(orders, ele, defaultOrder);
		}
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;
			for (Object ele : array)
				convertToCategoryOrders(orders, ele, defaultOrder);
		}
		else
			orders.add(convertToInt(obj, defaultOrder));
	}

	/**
	 * 将对象转换为指定枚举类型的对象。
	 * 
	 * @param obj
	 * @param enumType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> T convertToEnum(Object obj, Class<T> enumType)
	{
		if (obj == null)
			return null;
		else if (enumType.isAssignableFrom(obj.getClass()))
			return (T) obj;
		else if (obj instanceof String)
		{
			String strVal = (String) obj;
	
			EnumSet<T> enumSet = EnumSet.allOf(enumType);
	
			for (T e : enumSet)
			{
				if (e.name().equalsIgnoreCase(strVal))
					return e;
			}
	
			return null;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ enumType.getName() + "] is not supported");
	}

	/**
	 * 将对象转换为{@linkplain Label}。
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Label convertToLabel(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Label)
			return (Label) obj;
		else if (obj instanceof String)
		{
			Label label = createLabel();
			label.setValue((String) obj);
	
			return label;
		}
		else if (obj instanceof Map<?, ?>)
		{
			Map<String, ?> map = (Map<String, ?>) obj;
	
			Label label = createLabel();
			label.setValue((String) map.get(Label.PROPERTY_VALUE));
	
			Object localeValues = map.get(Label.PROPERTY_LOCALE_VALUES);
			if(localeValues == null)
				;
			else if (localeValues instanceof Map<?, ?>)
				label.setLocaleValues((Map<String, String>)localeValues);
			else
				throw new UnsupportedOperationException("Convert object of type [" + localeValues.getClass().getName() + "] to ["
						+ Label.class.getName() + ".localeValues] is not supported");
	
			return label;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Label.class.getName() + "] is not supported");
	}

	/**
	 * 将对象转换为布尔值。
	 * 
	 * @param obj
	 * @param defaultValue
	 * @return
	 */
	protected boolean convertToBoolean(Object obj, boolean defaultValue)
	{
		if (obj == null)
			return defaultValue;
		else if (obj instanceof Boolean)
			return ((Boolean) obj).booleanValue();
		else if (obj instanceof String)
		{
			String str = (String) obj;
			return ("1".equals(str) || "true".equalsIgnoreCase(str));
		}
		else
			throw new UnsupportedOperationException(
					"Convert object [" + obj + "] to [" + boolean.class.getName() + "] is not supported");
	}

	protected int convertToInt(Object obj, int defaultValue)
	{
		if (obj == null)
			return defaultValue;
		else if (obj instanceof Number)
			return ((Number) obj).intValue();
		else if (obj instanceof String)
		{
			try
			{
				return Integer.parseInt((String) obj);
			}
			catch (Exception e)
			{
				return defaultValue;
			}
		}
		else
			throw new UnsupportedOperationException(
					"Convert object [" + obj + "] to [" + Integer.class.getName() + "] is not supported");
	}
	
	protected Label createLabel()
	{
		return new Label();
	}

	protected ChartPluginAttribute createChartPluginAttribute()
	{
		return new ChartPluginAttribute();
	}

	protected DataSign createDataSign()
	{
		return new DataSign();
	}

	protected Category createCategory()
	{
		return new Category();
	}

	protected Group createGroup()
	{
		return new Group();
	}

	protected ChartPluginDataSetRange createChartPluginDataSetRange()
	{
		return new ChartPluginDataSetRange();
	}

	protected Range createRange()
	{
		return new Range();
	}
}
