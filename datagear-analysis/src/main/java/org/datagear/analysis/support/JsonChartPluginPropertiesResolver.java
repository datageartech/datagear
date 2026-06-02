/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginCategoryInfo;
import org.datagear.analysis.ChartPluginConfigForm;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginDataSetRange.Range;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.DataSignSpec;
import org.datagear.analysis.FullnameSpec;
import org.datagear.analysis.NameAware;
import org.datagear.analysis.form.AbstractFormProperty;
import org.datagear.analysis.form.Form;
import org.datagear.analysis.form.FormProperty;
import org.datagear.analysis.form.FormPropertyGroup;
import org.datagear.analysis.form.InputFormProperty;
import org.datagear.analysis.form.ObjectFormProperty;
import org.datagear.analysis.form.PropertyInputType;
import org.datagear.analysis.form.PropertyType;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Labeled;
import org.datagear.util.i18n.Localizable;

/**
 * JSON {@linkplain ChartPlugin}属性解析器。
 * <p>
 * 此类从JSON解析{@linkplain ChartPlugin}对象的属性：
 * </p>
 * <code>
 * <pre>
 * {
 *   id : "...",
 *   nameLabel : "..." 、 { value : "...", localeValues : { "zh" : "...", "en" : "..." }},
 *   descLabel : "..." 、 { ... },
 *   icons : "..." 、 { "light" : "icons/light.png", "dark" : "icons/dark.png" },
 *   configForm : { ... },
 *   dataSignSpec: { dataSigns: [ ... ] }、[ ... ],
 *   dataSetRange: 数值 、 "none" 、 { ... },
 *   version : "...",
 *   order: 整数值,
 *   categoryInfos: ...,
 *   author: "...",
 *   contact: "...",
 *   issueDate: "...",
 *   platformVersion: "...",
 *   additions: { "...": ..., ... }
 * }
 * </pre>
 * </code>
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginPropertiesResolver<T extends AbstractChartPlugin>
{
	public static final String JSON_PROPERTY_ID = ChartPlugin.PROPERTY_ID;
	public static final String JSON_PROPERTY_NAME_LABEL = ChartPlugin.PROPERTY_NAME_LABEL;
	public static final String JSON_PROPERTY_DESC_LABEL = ChartPlugin.PROPERTY_DESC_LABEL;
	public static final String JSON_PROPERTY_DATA_SIGN_SPEC = ChartPlugin.PROPERTY_DATA_SIGN_SPEC;
	public static final String JSON_PROPERTY_CONFIG_FORM = ChartPlugin.PROPERTY_CONFIG_FORM;
	public static final String JSON_PROPERTY_DATA_SET_RANGE = ChartPlugin.PROPERTY_DATA_SET_RANGE;
	public static final String JSON_PROPERTY_VERSION = ChartPlugin.PROPERTY_VERSION;
	public static final String JSON_PROPERTY_ORDER = ChartPlugin.PROPERTY_ORDER;
	public static final String JSON_PROPERTY_CATEGORY_INFOS = ChartPlugin.PROPERTY_CATEGORY_INFOS;
	public static final String JSON_PROPERTY_AUTHOR = ChartPlugin.PROPERTY_AUTHOR;
	public static final String JSON_PROPERTY_CONTACT = ChartPlugin.PROPERTY_CONTACT;
	public static final String JSON_PROPERTY_ISSUE_DATE = ChartPlugin.PROPERTY_ISSUE_DATE;
	public static final String JSON_PROPERTY_ICONS = "icons";
	public static final String JSON_PROPERTY_ADDITIONS = ChartPlugin.PROPERTY_ADDITIONS;

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPlugin.categories}格式
	 */
	@Deprecated
	public static final String JSON_PROPERTY_CATEGORIES = "categories";

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPlugin.categoryOrders}格式
	 */
	@Deprecated
	public static final String JSON_PROPERTY_CATEGORY_ORDERS = "categoryOrders";

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPlugin.dataSigns}格式
	 */
	@Deprecated
	public static final String JSON_PROPERTY_DATA_SIGNS = "dataSigns";

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPlugin.attributes}格式
	 */
	@Deprecated
	public static final String JSON_PROPERTY_ATTRIBUTES = "attributes";

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPluginAttribute.group}格式
	 */
	@Deprecated
	public static final String JSON_PROPERTY_INPUT_ATTR_GROUP = "group";

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPluginAttribute.group}格式
	 */
	@Deprecated
	public static final String INPUT_PROPERTY_ADDITION_OLD_GROUP = ChartDefinition.BUILTIN_NAME_PREFIX
			+ "GROUP_FOR_5_5_0";

	/**
	 * 3.0.1版本的单类别属性名，已在3.1.0版本中被{@linkplain #JSON_PROPERTY_CATEGORIES}代替。
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static final String JSON_PROPERTY_CATEGORY_3_0_1 = "category";

	/**
	 * {@linkplain #JSON_PROPERTY_DATA_SET_RANGE}属性的特殊值：{@code "none"}
	 * <p>
	 * 此值表示{@linkplain ChartPluginDataSetRange}的值为：<code>{ main: { min: 0, max: 0 }, attachment: { min: 0, max: 0 } }</code>
	 * </p>
	 */
	public static final String DATA_SET_RANGE_NONE = "none";

	private T chartPlugin;

	public JsonChartPluginPropertiesResolver()
	{
		super();
	}

	public JsonChartPluginPropertiesResolver(T chartPlugin)
	{
		super();
		this.chartPlugin = chartPlugin;
	}

	public T getChartPlugin()
	{
		return chartPlugin;
	}

	public void setChartPlugin(T chartPlugin)
	{
		this.chartPlugin = chartPlugin;
	}

	/**
	 * 从映射表解析并设置{@linkplain #getChartPlugin()}属性。
	 * <p>
	 * 它会进行必要的类型转换。
	 * </p>
	 * 
	 * @param properties
	 * @return {@linkplain #getChartPlugin()}
	 */
	public T resolveProperties(Map<String, ?> properties)
	{
		T chartPlugin = getChartPlugin();

		chartPlugin.setId(convertToString(properties.get(JSON_PROPERTY_ID)));
		chartPlugin.setNameLabel(convertToLabel(properties.get(JSON_PROPERTY_NAME_LABEL)));
		chartPlugin.setDescLabel(convertToLabel(properties.get(JSON_PROPERTY_DESC_LABEL)));
		chartPlugin.setIcons(convertToIcons(properties.get(JSON_PROPERTY_ICONS)));

		if (properties.containsKey(JSON_PROPERTY_CONFIG_FORM))
			chartPlugin.setConfigForm(convertToConfigForm(properties.get(JSON_PROPERTY_CONFIG_FORM)));
		else if (properties.containsKey(JSON_PROPERTY_ATTRIBUTES))
			chartPlugin.setConfigForm(convertToConfigFormForV5_5_0(properties.get(JSON_PROPERTY_ATTRIBUTES)));

		if(properties.containsKey(JSON_PROPERTY_DATA_SIGN_SPEC))
			chartPlugin.setDataSignSpec(convertToDataSignSpec(properties.get(JSON_PROPERTY_DATA_SIGN_SPEC)));
		else if (properties.containsKey(JSON_PROPERTY_DATA_SIGNS))
			chartPlugin.setDataSignSpec(convertToDataSignSpecForV_5_5_0(properties.get(JSON_PROPERTY_DATA_SIGNS)));

		chartPlugin.setDataSetRange(convertToDataSetRange(properties.get(JSON_PROPERTY_DATA_SET_RANGE)));
		chartPlugin.setVersion(convertToString(properties.get(JSON_PROPERTY_VERSION)));
		chartPlugin.setOrder(convertToInt(properties.get(JSON_PROPERTY_ORDER), chartPlugin.getOrder()));

		if (properties.containsKey(JSON_PROPERTY_CATEGORY_INFOS))
			chartPlugin.setCategoryInfos(convertToCategoryInfos(properties.get(JSON_PROPERTY_CATEGORY_INFOS)));
		else
		{
			Object categories = properties.get(JSON_PROPERTY_CATEGORIES);
			if (categories == null)
				categories = properties.get(JSON_PROPERTY_CATEGORY_3_0_1);

			Object categoryOrders = properties.get(JSON_PROPERTY_CATEGORY_ORDERS);

			chartPlugin.setCategoryInfos(convertToCategoryInfosForV5_5_0(categories, categoryOrders));
		}

		chartPlugin.setAuthor(convertToString(properties.get(JSON_PROPERTY_AUTHOR)));
		chartPlugin.setContact(convertToString(properties.get(JSON_PROPERTY_CONTACT)));
		chartPlugin.setIssueDate(convertToString(properties.get(JSON_PROPERTY_ISSUE_DATE)));
		chartPlugin.setAdditions(convertToAdditions(properties.get(JSON_PROPERTY_ADDITIONS)));

		return chartPlugin;
	}

	/**
	 * 从JSON字符串解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJson
	 * @return {@linkplain #getChartPlugin()}
	 * @throws IOException
	 */
	public T resolveProperties(String pluginJson) throws IOException
	{
		return resolveProperties(pluginJson, null, null);
	}

	/**
	 * 从JSON字符串解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJson
	 * @param dataSignsJson
	 *            允许{@code null}
	 * @param configFormJson
	 *            允许{@code null}
	 * @return
	 * @throws IOException
	 */
	public T resolveProperties(String pluginJson, String dataSignsJson, String configFormJson) throws IOException
	{
		Reader pluginIn = new StringReader(pluginJson);
		Reader dataSignsIn = (StringUtil.isEmpty(dataSignsJson) ? null : new StringReader(dataSignsJson));
		Reader configFormIn = (StringUtil.isEmpty(configFormJson) ? null : new StringReader(configFormJson));

		try
		{
			return resolveProperties(pluginIn, dataSignsIn, configFormIn);
		}
		finally
		{
			IOUtil.close(pluginIn);
			IOUtil.close(dataSignsIn);
			IOUtil.close(configFormIn);
		}
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJsonIn
	 * @return {@linkplain #getChartPlugin()}
	 * @throws IOException
	 */
	public T resolveProperties(Reader pluginJsonIn) throws IOException
	{
		return resolveProperties(pluginJsonIn, null, null);
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJsonIn
	 * @param dataSignsIn
	 *            允许{@code null}
	 * @param configFormIn
	 *            允许{@code null}
	 * @return {@linkplain #getChartPlugin()}
	 * @throws IOException
	 */
	public T resolveProperties(Reader pluginJsonIn, Reader dataSignsIn, Reader configFormIn)
			throws IOException
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = JsonSupport.parseNonStardand(pluginJsonIn, Map.class);

		if (dataSignsIn != null)
		{
			Object dataSigns = JsonSupport.parseNonStardand(dataSignsIn, List.class);
			properties.put(JSON_PROPERTY_DATA_SIGNS, dataSigns);
		}

		if (configFormIn != null)
		{
			Object configForm = JsonSupport.parseNonStardand(configFormIn, Map.class);
			properties.put(JSON_PROPERTY_CONFIG_FORM, configForm);
		}

		return resolveProperties(properties);
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJsonIn
	 * @param encoding
	 * @return {@linkplain #getChartPlugin()}
	 * @throws IOException
	 */
	public T resolveProperties(InputStream pluginJsonIn, String encoding)
			throws IOException
	{
		return resolveProperties(pluginJsonIn, null, null, encoding);
	}

	/**
	 * 从JSON输入流解析并设置{@linkplain #getChartPlugin()}属性。
	 * 
	 * @param pluginJsonIn
	 * @param dataSignsIn
	 *            允许{@code null}
	 * @param configFormIn
	 *            允许{@code null}
	 * @param encoding
	 * @return {@linkplain #getChartPlugin()}
	 * @throws IOException
	 */
	public T resolveProperties(InputStream pluginJsonIn, InputStream dataSignsIn, InputStream configFormIn,
			String encoding) throws IOException
	{
		Reader pluginReader = IOUtil.getReader(pluginJsonIn, encoding);
		Reader dataSignsReader = (dataSignsIn == null ? null : IOUtil.getReader(dataSignsIn, encoding));
		Reader configFormReader = (configFormIn == null ? null : IOUtil.getReader(configFormIn, encoding));
		return resolveProperties(pluginReader, dataSignsReader, configFormReader);
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
	protected Map<String, String> convertToIcons(Object obj)
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
					"Convert object of type [" + obj.getClass().getName() + "] to icon map unsupported");
	}

	protected DataSignSpec convertToDataSignSpec(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof DataSignSpec)
			return (DataSignSpec) obj;
		else if ((obj instanceof Object[]) || (obj instanceof Collection<?>))
		{
			DataSignSpec spec = createDataSignSpec();
			spec.setDataSigns(convertToDataSigns(obj, null));
			return spec;
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			DataSignSpec spec = createDataSignSpec();

			spec.setDataSigns(convertToDataSigns(map.get(DataSignSpec.PROPERTY_DATA_SIGNS), null));
			spec.setAdditions(convertToAdditions(map.get(DataSign.PROPERTY_ADDITIONS)));

			return spec;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ DataSignSpec.class.getName() + "] unsupported");
	}

	/**
	 * 将{@code 5.5.0}版的对象转换为{@linkplain DataSignSpec}。
	 * <p>
	 * 支持格式如下：
	 * <p>
	 * <code>[ { ... }, ... ]</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected DataSignSpec convertToDataSignSpecForV_5_5_0(Object obj)
	{
		List<DataSign> dataSigns = convertToDataSigns(obj, null);

		if (dataSigns == null)
			return null;

		DataSignSpec spec = new DataSignSpec(dataSigns);
		return spec;
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
	 * @param parent
	 *            允许{@code null}
	 * @return
	 */
	protected List<DataSign> convertToDataSigns(Object obj, DataSign parent)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;
	
			List<DataSign> dataSigns = new ArrayList<>();
	
			for (Object ele : array)
			{
				DataSign dataSign = convertToDataSign(ele, parent);
	
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
	
			return convertToDataSigns(array, parent);
		}
		else
		{
			Object[] array = new Object[] { obj };
			return convertToDataSigns(array, parent);
		}
	}

	/**
	 * 将对象转换为{@linkplain DataSign}。
	 * 
	 * @param obj
	 * @param parent
	 *            允许{@code null}
	 * @return
	 */
	protected DataSign convertToDataSign(Object obj, DataSign parent)
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
			if (StringUtil.isEmpty(name))
				return null;

			String fullname = (String) map.get(DataSign.PROPERTY_FULLNAME);
			if(StringUtil.isEmpty(fullname))
				fullname = FullnameSpec.toFullname(name, (parent == null ? null : parent.getFullname()));
	
			DataSign dataSign = createDataSign();

			dataSign.setName(name);
			dataSign.setFullname(fullname);
			dataSign.setTargets(convertToDataSignTargets(map.get(DataSign.PROPERTY_TARGETS), parent));
			dataSign.setRequired(convertToDataSignRequired(map.get(DataSign.PROPERTY_REQUIRED)));
			dataSign.setMultiple(convertToDataSignMultiple(map.get(DataSign.PROPERTY_MULTIPLE)));
			dataSign.setNameLabel(convertToLabel(map.get(DataSign.PROPERTY_NAME_LABEL)));
			dataSign.setDescLabel(convertToLabel(map.get(DataSign.PROPERTY_DESC_LABEL)));
			dataSign.setAdditions(convertToAdditions(map.get(DataSign.PROPERTY_ADDITIONS)));
			dataSign.setChildren(convertToDataSigns(map.get(DataSign.PROPERTY_CHILDREN), dataSign));
			dataSign.setFieldMatcher(map.get(DataSign.PROPERTY_FIELD_MATCHER));
	
			return dataSign;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ DataSign.class.getName() + "] unsupported");
	}

	/**
	 * 转换为{@linkplain DataSign#getTargets()}。
	 * <p>
	 * 支持格式：{@code "..."}、{@code [ "...", ... ]}
	 * </p>
	 * 
	 * @param v
	 * @param parent
	 * @return
	 */
	protected String[] convertToDataSignTargets(Object v, DataSign parent)
	{
		String[] targets;

		// 设为默认值，以兼容<=5.3.0版本逻辑
		if(v == null)
		{
			targets = DataSign.TARGETS_FIELDS;
		}
		else if (v instanceof String)
		{
			targets = convertToDataSignTargets(Arrays.asList(v), parent);
		}
		else if (v instanceof Object[])
		{
			targets = convertToDataSignTargets(Arrays.asList((Object[]) v), parent);
		}
		else if (v instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) v;
			List<String> targetList = new ArrayList<String>(collection.size());

			for (Object ele : collection)
			{
				if (ele instanceof String)
				{
					String eleStr = (String) ele;
					String target = DataSign.normalizeTarget(eleStr, eleStr);
					targetList.add(target);
				}
			}

			targets = targetList.toArray(new String[targetList.size()]);
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + v.getClass().getName() + "] to ["
					+ DataSign.class.getName() + ".targets] unsupported");

		return targets;
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
	 * 将对象转换为{@linkplain ChartPluginConfigForm}。
	 * <p>
	 * 支持格式如下：
	 * <p>
	 * <code>{ ... }</code>
	 * </p>
	 * 
	 * @return
	 */
	protected ChartPluginConfigForm convertToConfigForm(Object obj)
	{
		ChartPluginConfigForm form = null;

		if (obj == null)
			form = null;
		else if (obj instanceof ChartPluginConfigForm)
			form = ((ChartPluginConfigForm) obj);
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			form = createChartPluginConfigForm();
			form.setProperties(convertToFormProperties(map.get(Form.PROPERTY_PROPERTIES)));
			form.setGroups(convertToFormPropertyGroups(map.get(Form.PROPERTY_GROUPS)));
			form.setNameLabel(convertToLabel(map.get(Form.PROPERTY_NAME_LABEL)));
			form.setDescLabel(convertToLabel(map.get(Form.PROPERTY_DESC_LABEL)));
			form.setAdditions(convertToAdditions(map.get(Form.PROPERTY_ADDITIONS)));
			form.setDefaultValue(map.get(Form.PROPERTY_DEFAULT_VALUE));
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginConfigForm.class.getName() + "] unsupported");

		return form;
	}

	/**
	 * 将{@code 5.5.0}版的对象转换为{@linkplain ChartPluginConfigForm}。
	 * <p>
	 * 支持格式如下：
	 * <p>
	 * <code>[ { ... }, ... ]</code>
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected ChartPluginConfigForm convertToConfigFormForV5_5_0(Object obj)
	{
		List<FormProperty> properties = convertToFormProperties(obj);

		if (properties == null)
			return null;

		ChartPluginConfigForm form = createChartPluginConfigForm();
		form.setProperties(properties);

		return form;
	}

	/**
	 * 将对象转换为{@linkplain FormProperty}列表。
	 * 
	 * @param obj
	 * @return
	 */
	protected List<FormProperty> convertToFormProperties(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<FormProperty> properties = new ArrayList<>();

			for (Object ele : array)
			{
				FormProperty property = convertToFormProperty(ele);

				if (property != null)
					properties.add(property);
			}

			if (properties.isEmpty())
				return null;

			return properties;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);

			return convertToFormProperties(array);
		}
		else
		{
			Object[] array = new Object[] { obj };
			return convertToFormProperties(array);
		}
	}

	/**
	 * 将对象转换为{@linkplain FormProperty}。
	 * 
	 * @param obj
	 * @return
	 */
	protected FormProperty convertToFormProperty(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof FormProperty)
		{
			FormProperty prop = (FormProperty) obj;

			if (StringUtil.isEmpty(prop.getName()))
				throw new IllegalArgumentException(FormProperty.class.getSimpleName() + ".name required");

			return prop;
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;
			String name = convertToString(map.get(FormProperty.PROPERTY_NAME));

			if (StringUtil.isEmpty(name))
				return null;

			String type = convertToString(map.get(FormProperty.PROPERTY_TYPE));
			type = PropertyType.normalize(type, type);

			boolean isObject = (PropertyType.OBJECT.equals(type)
					|| (StringUtil.isEmpty(type) && map.containsKey(ObjectFormProperty.PROPERTY_PROPERTIES)));

			AbstractFormProperty prop;

			if (isObject)
				prop = createObjectFormProperty();
			else
				prop = createInputFormProperty();

			prop.setName(name);
			prop.setNameLabel(convertToLabel(map.get(FormProperty.PROPERTY_NAME_LABEL)));
			prop.setDescLabel(convertToLabel(map.get(FormProperty.PROPERTY_DESC_LABEL)));
			prop.setRequired(convertToFormPropertyRequired(map.get(FormProperty.PROPERTY_REQUIRED)));
			prop.setArray(convertToFormPropertyArray(map.get(FormProperty.PROPERTY_ARRAY)));
			prop.setAdditions(convertToAdditions(map.get(FormProperty.PROPERTY_ADDITIONS)));
			prop.setDefaultValue(map.get(FormProperty.PROPERTY_DEFAULT_VALUE));

			if (isObject)
			{
				ObjectFormProperty objProp = (ObjectFormProperty) prop;

				objProp.setProperties(convertToFormProperties(map.get(ObjectFormProperty.PROPERTY_PROPERTIES)));
				objProp.setGroups(convertToFormPropertyGroups(map.get(ObjectFormProperty.PROPERTY_GROUPS)));
			}
			else
			{
				InputFormProperty inputProp = (InputFormProperty) prop;

				inputProp.setType(convertToInputFormPropertyType(type));
				inputProp.setInputType(
						convertToInputFormPropertyInputType(map.get(InputFormProperty.PROPERTY_INPUT_TYPE)));
				inputProp.setInputPayload(
						convertToInputFormPropertyInputPayload(map.get(InputFormProperty.PROPERTY_INPUT_PAYLOAD)));
				inputProp.setDefaultValue(map.get(FormProperty.PROPERTY_DEFAULT_VALUE));

				// 将旧版分组信息存入附加数据中，便于前端恢复旧版格式
				Group group = convertToInputFormPropertyGroupForV5_5_0(map.get(JSON_PROPERTY_INPUT_ATTR_GROUP));
				if (group != null)
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> additions = (Map<String, Object>) inputProp.getAdditions();
					additions = (additions == null ? new HashMap<>()
							: (additions instanceof HashMap<?, ?> ? additions : new HashMap<>(additions)));
					additions.put(INPUT_PROPERTY_ADDITION_OLD_GROUP, group);
					inputProp.setAdditions(additions);
				}
			}

			return prop;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ FormProperty.class.getName() + "] unsupported");
	}

	protected boolean convertToFormPropertyRequired(Object v)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		boolean dftValue = false;
		return convertToBoolean(v, dftValue);
	}

	protected boolean convertToFormPropertyArray(Object v)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		boolean dftValue = false;
		return convertToBoolean(v, dftValue);
	}

	protected String convertToInputFormPropertyType(String str)
	{
		// 不要修改这里的默认值，因为会影响插件规范
		String dftValue = PropertyType.STRING;
		return PropertyType.normalize(str, dftValue);
	}

	protected String convertToInputFormPropertyInputType(Object obj)
	{
		if(obj == null)
			return  null;
		else if (obj instanceof String)
			return (String) obj;
		else
			return PropertyInputType.TEXT;
	}

	protected Object convertToInputFormPropertyInputPayload(Object obj)
	{
		return obj;
	}

	protected List<FormPropertyGroup> convertToFormPropertyGroups(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<FormPropertyGroup> groups = new ArrayList<>();

			for (Object ele : array)
			{
				FormPropertyGroup group = convertToFormPropertyGroup(ele);

				if (group != null)
					groups.add(group);
			}

			if (groups.isEmpty())
				return null;

			return groups;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);

			return convertToFormPropertyGroups(array);
		}
		else
		{
			Object[] array = new Object[] { obj };
			return convertToFormPropertyGroups(array);
		}
	}

	protected FormPropertyGroup convertToFormPropertyGroup(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof FormPropertyGroup)
			return (FormPropertyGroup) obj;
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			FormPropertyGroup group = createFormPropertyGroup();

			group.setNameLabel(convertToLabel(map.get(FormPropertyGroup.PROPERTY_NAME_LABEL)));
			group.setDescLabel(convertToLabel(map.get(FormPropertyGroup.PROPERTY_DESC_LABEL)));
			group.setNames(convertToFormPropertyGroupNames(map.get(FormPropertyGroup.PROPERTY_NAMES)));
			group.setAdditions(convertToAdditions(map.get(FormPropertyGroup.PROPERTY_ADDITIONS)));

			return group;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ FormPropertyGroup.class.getName() + "] unsupported");
	}

	protected List<String> convertToFormPropertyGroupNames(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof String)
		{
			return Arrays.asList((String) obj);
		}
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<String> names = new ArrayList<>();

			for (Object ele : array)
			{
				if (ele != null && (ele instanceof String))
					names.add((String) ele);
			}

			if (names.isEmpty())
				return null;

			return names;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);
			return convertToFormPropertyGroupNames(array);
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ FormPropertyGroup.class.getName() + ".names] unsupported");
	}

	protected Group convertToInputFormPropertyGroupForV5_5_0(Object obj)
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
					+ Group.class.getName() + "] unsupported");
	}

	/**
	 * 将对象转换为{@linkplain ChartPluginDataSetRange}。
	 * <p>
	 * 支持如下四种格式：
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
	 * 定义{@linkplain #DATA_SET_RANGE_NONE}表示的格式：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * "none"
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
		else if ((obj instanceof String) && DATA_SET_RANGE_NONE.equalsIgnoreCase((String) obj))
		{
			ChartPluginDataSetRange dsr = createChartPluginDataSetRange();

			Range main = createRange();
			main.setMin(0);
			main.setMax(0);

			Range attachment = createRange();
			attachment.setMin(0);
			attachment.setMax(0);

			dsr.setMain(main);
			dsr.setAttachment(attachment);

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
					+ ChartPluginDataSetRange.class.getName() + "] unsupported");
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

	protected List<ChartPluginCategoryInfo> convertToCategoryInfos(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof ChartPluginCategoryInfo)
		{
			return Arrays.asList((ChartPluginCategoryInfo) obj);
		}
		// "..." 类别名
		else if (obj instanceof String)
		{
			return convertToCategoryInfos(Arrays.asList(obj));
		}
		// { }
		else if (obj instanceof Map<?, ?>)
		{
			return convertToCategoryInfos(Arrays.asList(obj));
		}
		// [ ... ]
		else if (obj instanceof Object[])
		{
			return convertToCategoryInfos(Arrays.asList((Object[]) obj));
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;

			List<ChartPluginCategoryInfo> categoryInfos = new ArrayList<>();

			for (Object o : collection)
			{
				ChartPluginCategoryInfo categoryInfo = convertToCategoryInfo(o);
				if (categoryInfo != null)
					categoryInfos.add(categoryInfo);
			}

			return categoryInfos;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginCategoryInfo.class.getName() + "] list unsupported");
	}

	protected List<ChartPluginCategoryInfo> convertToCategoryInfosForV5_5_0(Object categoriesObj, Object categoryOrdersObj)
	{
		if(categoriesObj == null)
			return null;
		
		List<Category> categories = convertToCategories(categoriesObj);

		if (categories == null)
			return null;

		List<Integer> categoryOrders = convertToCategoryOrders(categoryOrdersObj);

		List<ChartPluginCategoryInfo> categoryInfos = new ArrayList<>();

		for (int i = 0; i < categories.size(); i++)
		{
			Category category = categories.get(i);
			Integer categoryOrder = (categoryOrders == null || categoryOrders.size() <= i ? null
					: categoryOrders.get(i));

			ChartPluginCategoryInfo categoryInfo = createCategoryInfo();
			categoryInfo.setCategory(category);
			if (categoryOrder != null)
				categoryInfo.setOrder(categoryOrder);

			categoryInfos.add(categoryInfo);
		}

		return categoryInfos;
	}

	protected ChartPluginCategoryInfo convertToCategoryInfo(Object obj)
	{
		if (obj == null)
			return null;

		// "..." 类别名
		if (obj instanceof String)
		{
			Category category = convertToCategory(obj);
			ChartPluginCategoryInfo categoryInfo = createCategoryInfo();
			categoryInfo.setCategory(category);

			return categoryInfo;
		}
		else if(obj instanceof Map<?, ?>)
		{
			ChartPluginCategoryInfo categoryInfo = null;

			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			// CategoryInfo
			if(map.containsKey(ChartPluginCategoryInfo.PROPERTY_CATEGORY))
			{
				Category category = convertToCategory(map.get(ChartPluginCategoryInfo.PROPERTY_CATEGORY));

				if (category != null)
				{
					categoryInfo = createCategoryInfo();
					categoryInfo.setCategory(category);
					categoryInfo.setOrder(convertToInt(map.get(ChartPluginCategoryInfo.PROPERTY_ORDER), 0));
				}
			}
			// Category
			else if (map.containsKey(Category.PROPERTY_NAME))
			{
				Category category = convertToCategory(obj);

				if (category != null)
				{
					categoryInfo = createCategoryInfo();
					categoryInfo.setCategory(category);
				}
			}

			return categoryInfo;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartPluginCategoryInfo.class.getName() + "] unsupported");
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
		if (obj == null)
			return null;

		if (obj instanceof Category)
		{
			return Arrays.asList((Category) obj);
		}
		else if ((obj instanceof String) || (obj instanceof Map<?, ?>))
		{
			Category category = convertToCategory(obj);
			return (category == null ? null : Arrays.asList(category));
		}
		else if (obj instanceof Object[])
		{
			return convertToCategories(Arrays.asList((Object[]) obj));
		}
		else if (obj instanceof Collection<?>)
		{
			List<Category> categories = new ArrayList<>();

			Collection<?> collection = (Collection<?>) obj;
			for (Object ele : collection)
			{
				Category category = convertToCategory(ele);
				if (category != null)
					categories.add(category);
			}

			return categories;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Category.class.getName() + "] list unsupported");
	}

	protected List<Integer> convertToCategoryOrders(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof Object[])
		{
			return convertToCategoryOrders(Arrays.asList((Object[]) obj));
		}
		else if (obj instanceof Collection<?>)
		{
			List<Integer> orders = new ArrayList<>();

			Collection<?> collection = (Collection<?>) obj;
			for (Object ele : collection)
				orders.add(convertToInt(ele, 0));

			return orders;
		}
		else
		{
			return Arrays.asList(convertToInt(obj, 0));
		}
	}

	protected Category convertToCategory(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof String)
		{
			Category category = createCategory();
			category.setName((String) obj);

			return category;
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			String name = (String) map.get(Category.PROPERTY_NAME);

			if (name == null)
				return null;

			Category category = createCategory();
			category.setName(name);

			category.setNameLabel(convertToLabel(map.get(Category.PROPERTY_NAME_LABEL)));
			category.setDescLabel(convertToLabel(map.get(Category.PROPERTY_DESC_LABEL)));
			category.setOrder(convertToInt(map.get(Category.PROPERTY_ORDER), category.getOrder()));

			return category;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Category.class.getName() + "] unsupported");
	}

	@SuppressWarnings("unchecked")
	protected Map<String, ?> convertToAdditions(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Map<?, ?>)
			return (Map<String, ?>) obj;
		else
			throw new UnsupportedOperationException(
					"Convert object of type [" + obj.getClass().getName() + "] to [additions] unsupported");
	}

	/**
	 * 将对象转换为指定枚举类型的对象。
	 * 
	 * @param obj
	 * @param enumType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E>> E convertToEnum(Object obj, Class<E> enumType)
	{
		if (obj == null)
			return null;
		else if (enumType.isAssignableFrom(obj.getClass()))
			return (E) obj;
		else if (obj instanceof String)
		{
			String strVal = (String) obj;
	
			EnumSet<E> enumSet = EnumSet.allOf(enumType);
	
			for (E e : enumSet)
			{
				if (e.name().equalsIgnoreCase(strVal))
					return e;
			}
	
			return null;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ enumType.getName() + "] unsupported");
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
						+ Label.class.getName() + ".localeValues] unsupported");
	
			return label;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Label.class.getName() + "] unsupported");
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
			return StringUtil.toBoolean(str);
		}
		else
			throw new UnsupportedOperationException(
					"Convert object [" + obj + "] to [" + boolean.class.getName() + "] unsupported");
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
			return defaultValue;
	}
	
	protected String convertToString(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof String)
			return (String) obj;
		else
			return obj.toString();
	}

	protected Label createLabel()
	{
		return new Label();
	}

	protected ChartPluginConfigForm createChartPluginConfigForm()
	{
		return new ChartPluginConfigForm();
	}

	protected ObjectFormProperty createObjectFormProperty()
	{
		return new ObjectFormProperty();
	}

	protected InputFormProperty createInputFormProperty()
	{
		return new InputFormProperty();
	}

	protected FormPropertyGroup createFormPropertyGroup()
	{
		return new FormPropertyGroup();
	}

	protected DataSignSpec createDataSignSpec()
	{
		return new DataSignSpec();
	}

	protected DataSign createDataSign()
	{
		return new DataSign();
	}

	protected ChartPluginCategoryInfo createCategoryInfo()
	{
		return new ChartPluginCategoryInfo();
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

	/**
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.Group}
	 */
	@Deprecated
	protected static class Group extends AbstractLabeled implements NameAware, Localizable, Serializable
	{
		private static final long serialVersionUID = 1L;

		public static final String PROPERTY_NAME = "name";
		public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
		public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
		public static final String PROPERTY_ORDER = "order";

		private String name;

		private int order = 0;

		public Group()
		{
			super();
		}

		public Group(String name)
		{
			super();
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		@Override
		public Group toLocale(Locale locale)
		{
			Group target = createEmpty();

			target.setName(this.name);
			target.setOrder(this.order);
			LabelUtil.concrete(this, target, locale);

			return target;
		}

		protected Group createEmpty()
		{
			return new Group();
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", nameLabel=" + getNameLabel() + ", descLabel="
					+ getDescLabel() + ", order=" + order + "]";
		}
	}
}