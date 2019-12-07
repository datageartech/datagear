/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperties;
import org.datagear.analysis.ChartProperty;
import org.datagear.analysis.Icon;
import org.datagear.analysis.PropertyType;
import org.datagear.analysis.RenderStyle;
import org.datagear.util.i18n.Label;

import com.alibaba.fastjson.JSON;

/**
 * JSON {@linkplain ChartPlugin}解析器。
 * <p>
 * 此类从JSON解析{@linkplain ChartPlugin}对象的属性：
 * </p>
 * <code>
 * <pre>
 * {
 * 	id : "...",
 * 	nameLabel : { value : "...", localeValues : { "zh" : "...", "en" : "..." }},
 * 	descLabel : { ... },
 * 	manualLabel : { ... },
 * 	icons : { "LIGHTNESS" : { location : "classpath:/.../.../icon.png" }, "DARK" : { location : "file:/.../.../icon.png" } },
 * 	chartProperties :  [ { ... }, ... ]
 * }
 * </pre>
 * </code>
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginResolver
{
	public static final String CHART_PLUGIN_ID = "id";

	public static final String CHART_PLUGIN_NAME_LABEL = "nameLabel";

	public static final String CHART_PLUGIN_DESC_LABEL = "descLabel";

	public static final String CHART_PLUGIN_MANUAL_LABEL = "manualLabel";

	public static final String CHART_PLUGIN_ICONS = "icons";

	public static final String CHART_PLUGIN_CHART_PROPERTIES = "chartProperties";

	public static final String LABEL_VALUE = "value";

	public static final String LABEL_LOCALE_VALUES = "localeValues";

	public static final String LOCATION_ICON_LOCATION = "location";

	public static final String CHART_PROPERTY_NAME = "name";

	public static final String CHART_PROPERTY_TYPE = "type";

	public static final String CHART_PROPERTY_NAME_LABEL = "nameLabel";

	public static final String CHART_PROPERTY_DESC_LABEL = "descLabel";

	public static final String CHART_PROPERTY_DEFAULT_VALUE = "defaultValue";

	public static final String CHART_PROPERTY_CONSTRAINTS = "constraints";

	private PropertyTypeValueConverter propertyTypeValueConverter = new PropertyTypeValueConverter();

	private ConcurrentMap<String, Locale> _localeCache = new ConcurrentHashMap<String, Locale>();

	public JsonChartPluginResolver()
	{
		super();
	}

	public PropertyTypeValueConverter getPropertyTypeValueConverter()
	{
		return propertyTypeValueConverter;
	}

	public void setPropertyTypeValueConverter(PropertyTypeValueConverter propertyTypeValueConverter)
	{
		this.propertyTypeValueConverter = propertyTypeValueConverter;
	}

	/**
	 * 从JSON输入流解析{@linkplain ChartPlugin}。
	 * <p>
	 * 它会进行类型转换。
	 * </p>
	 * 
	 * @param json
	 * @return
	 */
	public Map<String, Object> resolveChartPluginProperties(String json)
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) JSON.parse(json);

		Map<String, Object> properties = new HashMap<String, Object>(map);

		properties.put(CHART_PLUGIN_NAME_LABEL, convertToLabel(map.get(CHART_PLUGIN_NAME_LABEL)));
		properties.put(CHART_PLUGIN_DESC_LABEL, convertToLabel(map.get(CHART_PLUGIN_DESC_LABEL)));
		properties.put(CHART_PLUGIN_MANUAL_LABEL, convertToLabel(map.get(CHART_PLUGIN_MANUAL_LABEL)));
		properties.put(CHART_PLUGIN_ICONS, convertToIcons(map.get(CHART_PLUGIN_ICONS)));
		properties.put(CHART_PLUGIN_CHART_PROPERTIES, convertToChartProperties(map.get(CHART_PLUGIN_CHART_PROPERTIES)));

		return properties;
	}

	/**
	 * 将映射表中的对应属性值设置到{@linkplain AbstractChartPlugin}中。
	 * 
	 * @param properties
	 * @param chartPlugin
	 */
	@SuppressWarnings("unchecked")
	public void setToChartPlugin(Map<String, ?> properties, AbstractChartPlugin<?> chartPlugin)
	{
		chartPlugin.setId((String) properties.get(CHART_PLUGIN_ID));
		chartPlugin.setNameLabel((Label) properties.get(CHART_PLUGIN_NAME_LABEL));
		chartPlugin.setDescLabel((Label) properties.get(CHART_PLUGIN_DESC_LABEL));
		chartPlugin.setManualLabel((Label) properties.get(CHART_PLUGIN_MANUAL_LABEL));
		chartPlugin.setIcons((Map<RenderStyle, Icon>) properties.get(CHART_PLUGIN_ICONS));
		chartPlugin.setChartProperties((ChartProperties) properties.get(CHART_PLUGIN_CHART_PROPERTIES));
	}

	/**
	 * 将对象转换为{@linkplain Label}。
	 * 
	 * @param obj
	 * @return
	 */
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
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			Label label = createLabel();
			label.setValue((String) map.get(LABEL_VALUE));

			Object localeValuesObj = map.get(LABEL_LOCALE_VALUES);
			if (localeValuesObj != null)
			{
				Map<Locale, String> localeValues = new HashMap<Locale, String>();

				@SuppressWarnings("unchecked")
				Map<String, String> stringLocaleValues = (Map<String, String>) localeValuesObj;

				for (Map.Entry<String, String> entry : stringLocaleValues.entrySet())
				{
					Locale locale = this._localeCache.get(entry.getKey());
					if (locale == null)
					{
						locale = Locale.forLanguageTag(entry.getKey());
						this._localeCache.putIfAbsent(entry.getKey(), locale);
					}

					localeValues.put(locale, entry.getValue());
				}

				label.setLocaleValues(localeValues);
			}

			return label;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + Label.class.getName() + "] is not supported");
	}

	/**
	 * 将对象转换为{@linkplain Icon}映射表。
	 * 
	 * @param obj
	 * @return
	 */
	protected Map<RenderStyle, Icon> convertToIcons(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof String)
		{
			Map<RenderStyle, Icon> icons = new HashMap<RenderStyle, Icon>();
			icons.put(RenderStyle.LIGHTNESS, convertToIcon(obj));

			return icons;
		}
		else if (obj instanceof Map<?, ?>)
		{
			Map<RenderStyle, Icon> icons = new HashMap<RenderStyle, Icon>();

			Map<?, ?> map = (Map<?, ?>) obj;

			for (Map.Entry<?, ?> entry : map.entrySet())
			{
				Object key = entry.getKey();
				RenderStyle renderStyle = convertToRenderStyle(key);

				if (renderStyle == null)
					continue;

				Icon icon = convertToIcon(entry.getValue());

				icons.put(renderStyle, icon);
			}

			return icons;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + Icon.class.getName() + "] map is not supported");
	}

	/**
	 * 将对象转换为{@linkplain Icon}。
	 * 
	 * @param obj
	 * @return
	 */
	protected Icon convertToIcon(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Icon)
			return (Icon) obj;
		else if (obj instanceof String)
		{
			LocationIcon icon = createLocationIcon();
			icon.setLocation((String) obj);

			return icon;
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, String>) obj;

			String location = (String) map.get(LOCATION_ICON_LOCATION);

			if (location == null)
				return null;

			LocationIcon icon = createLocationIcon();
			icon.setLocation(location);

			return icon;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + Icon.class.getName() + "] is not supported");
	}

	/**
	 * 将对象转换为{@linkplain RenderStyle}。
	 * 
	 * @param obj
	 * @return
	 */
	protected RenderStyle convertToRenderStyle(Object obj)
	{
		return convertToEnum(obj, RenderStyle.class);
	}

	/**
	 * 将对象转换为{@linkplain ChartProperties}。
	 * 
	 * @param obj
	 * @return
	 */
	protected ChartProperties convertToChartProperties(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof ChartProperties)
			return (ChartProperties) obj;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<ChartProperty> list = new ArrayList<ChartProperty>(array.length);

			for (Object ele : array)
			{
				ChartProperty chartProperty = convertToChartProperty(ele);

				if (chartProperty != null)
					list.add(chartProperty);
			}

			if (list.isEmpty())
				return null;

			ChartProperties chartProperties = createChartProperties();
			chartProperties.setProperties(list);

			return chartProperties;
		}
		else if (obj instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) obj;
			Object[] array = new Object[collection.size()];
			collection.toArray(array);

			return convertToChartProperties(array);
		}
		else
		{
			Object[] array = new Object[] { obj };

			return convertToChartProperties(array);
		}
	}

	/**
	 * 将对象转换为{@linkplain ChartProperty}。
	 * 
	 * @param obj
	 * @return
	 */
	protected ChartProperty convertToChartProperty(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof ChartProperty)
			return (ChartProperty) obj;
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, String>) obj;

			String name = (String) map.get(CHART_PROPERTY_NAME);
			if (name == null || name.isEmpty())
				return null;

			ChartProperty chartProperty = createChartProperty();
			chartProperty.setName(name);

			PropertyType type = convertToPropertyType(map.get(CHART_PROPERTY_TYPE));
			if (type == null)
				type = PropertyType.STRING;

			chartProperty.setType(type);
			chartProperty.setNameLabel(convertToLabel(map.get(CHART_PROPERTY_NAME_LABEL)));
			chartProperty.setDescLabel(convertToLabel(map.get(CHART_PROPERTY_DESC_LABEL)));
			chartProperty.setDefaultValue(convertToPropertyTypeValue(type, map.get(CHART_PROPERTY_DEFAULT_VALUE)));

			// TODO 解析constraints属性

			return chartProperty;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + ChartProperty.class.getName() + "] is not supported");
	}
	
	/**
	 * 将对象转换为{@linkplain PropertyType}。
	 * 
	 * @param obj
	 * @return
	 */
	protected PropertyType convertToPropertyType(Object obj)
	{
		return convertToEnum(obj, PropertyType.class);
	}
	
	/**
	 * 将对象转换为{@linkplain PropertyType}所描述类型的值。
	 * 
	 * @param propertyType
	 * @param obj
	 * @return
	 */
	protected Object convertToPropertyTypeValue(PropertyType propertyType, Object obj)
	{
		return this.propertyTypeValueConverter.convert(propertyType, obj);
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
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + enumType.getName() + "] is not supported");
	}

	protected Label createLabel()
	{
		return new Label();
	}

	protected LocationIcon createLocationIcon()
	{
		return new LocationIcon();
	}

	protected ChartProperties createChartProperties()
	{
		return new ChartProperties();
	}

	protected ChartProperty createChartProperty()
	{
		return new ChartProperty();
	}
}
