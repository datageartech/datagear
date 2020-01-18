/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperty;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.PropertyType;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.constraint.Constraint;
import org.datagear.analysis.constraint.Max;
import org.datagear.analysis.constraint.MaxLength;
import org.datagear.analysis.constraint.Min;
import org.datagear.analysis.constraint.MinLength;
import org.datagear.analysis.constraint.Required;
import org.datagear.util.IOUtil;
import org.datagear.util.i18n.Label;

import com.alibaba.fastjson.JSON;

/**
 * JSON {@linkplain ChartPlugin}属性解析器。
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
 * 	icons : { "LIGHT" : "icons/light.png", "DARK" : "icons/dark.png" },
 * 	chartProperties :  [ { ... }, ... ],
 * 	dataSigns : [ { ... }, ... ],
 * 	version : "...",
 * 	order: 1
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
	public static final String CHART_PLUGIN_ID = "id";

	public static final String CHART_PLUGIN_NAME_LABEL = "nameLabel";

	public static final String CHART_PLUGIN_DESC_LABEL = "descLabel";

	public static final String CHART_PLUGIN_MANUAL_LABEL = "manualLabel";

	public static final String CHART_PLUGIN_ICONS = "icons";

	public static final String CHART_PLUGIN_CHART_PROPERTIES = "chartProperties";

	public static final String CHART_PLUGIN_DATA_SIGNS = "dataSigns";

	public static final String CHART_PLUGIN_VERSION = "version";

	public static final String CHART_PLUGIN_ORDER = "order";

	public static final String LABEL_VALUE = "value";

	public static final String LABEL_LOCALE_VALUES = "localeValues";

	public static final String LOCATION_ICON_LOCATION = "location";

	public static final String CHART_PROPERTY_NAME = "name";

	public static final String CHART_PROPERTY_TYPE = "type";

	public static final String CHART_PROPERTY_NAME_LABEL = "nameLabel";

	public static final String CHART_PROPERTY_DESC_LABEL = "descLabel";

	public static final String CHART_PROPERTY_DEFAULT_VALUE = "defaultValue";

	public static final String CHART_PROPERTY_CONSTRAINTS = "constraints";

	public static final String DATA_SIGN_PROPERTY_NAME = "name";

	public static final String DATA_SIGN_PROPERTY_OCCUR_REQUIRED = "occurRequired";

	public static final String DATA_SIGN_PROPERTY_OCCUR_MULTIPLE = "occurMultiple";

	public static final String DATA_SIGN_PROPERTY_NAME_LABEL = "nameLabel";

	public static final String DATA_SIGN_PROPERTY_DESC_LABEL = "descLabel";

	private PropertyTypeValueConverter propertyTypeValueConverter = new PropertyTypeValueConverter();

	private ConcurrentMap<String, Locale> _localeCache = new ConcurrentHashMap<String, Locale>();

	public JsonChartPluginPropertiesResolver()
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
	 * 从JSON字符串解析{@linkplain ChartPlugin}。
	 * <p>
	 * 它会进行必要的类型转换。
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
		properties.put(CHART_PLUGIN_DATA_SIGNS, convertToDataSigns(map.get(CHART_PLUGIN_DATA_SIGNS)));

		return properties;
	}

	/**
	 * 从JSON输入流解析{@linkplain ChartPlugin}。
	 * 
	 * @param jsonReader
	 * @return
	 * @throws IOException
	 */
	public Map<String, Object> resolveChartPluginProperties(Reader jsonReader) throws IOException
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

		if (writer != null)
			json = writer.toString();

		if (json == null || json.isEmpty())
			return new HashMap<String, Object>();

		return resolveChartPluginProperties(json);
	}

	/**
	 * 从JSON输入流解析{@linkplain ChartPlugin}。
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public Map<String, Object> resolveChartPluginProperties(InputStream in, String encoding) throws IOException
	{
		Reader reader = IOUtil.getReader(in, encoding);
		return resolveChartPluginProperties(reader);
	}

	/**
	 * 将映射表中的对应属性值设置到{@linkplain AbstractChartPlugin}中。
	 * 
	 * @param chartPlugin
	 * @param properties
	 */
	@SuppressWarnings("unchecked")
	public void setChartPluginProperties(AbstractChartPlugin<?> chartPlugin, Map<String, ?> properties)
	{
		chartPlugin.setId((String) properties.get(CHART_PLUGIN_ID));
		chartPlugin.setNameLabel((Label) properties.get(CHART_PLUGIN_NAME_LABEL));
		chartPlugin.setDescLabel((Label) properties.get(CHART_PLUGIN_DESC_LABEL));
		chartPlugin.setManualLabel((Label) properties.get(CHART_PLUGIN_MANUAL_LABEL));
		chartPlugin.setIcons((Map<RenderStyle, Icon>) properties.get(CHART_PLUGIN_ICONS));
		chartPlugin.setChartProperties((List<ChartProperty>) properties.get(CHART_PLUGIN_CHART_PROPERTIES));
		chartPlugin.setDataSigns((List<DataSign>) properties.get(CHART_PLUGIN_DATA_SIGNS));
		chartPlugin.setVersion((String) properties.get(CHART_PLUGIN_VERSION));

		Integer order = null;

		Object orderObj = properties.get(CHART_PLUGIN_ORDER);
		if (orderObj instanceof Number)
			order = ((Number) orderObj).intValue();
		else if (orderObj instanceof String)
		{
			try
			{
				order = Integer.parseInt((String) orderObj);
			}
			catch (Exception e)
			{
			}
		}

		if (order != null)
			chartPlugin.setOrder(order);
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
						locale = stringToLocale(entry.getKey());
						this._localeCache.putIfAbsent(entry.getKey(), locale);
					}

					localeValues.put(locale, entry.getValue());
				}

				label.setLocaleValues(localeValues);
			}

			return label;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Label.class.getName() + "] is not supported");
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
			icons.put(RenderStyle.LIGHT, convertToIcon(obj));

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
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Icon.class.getName() + "] map is not supported");
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
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Icon.class.getName() + "] is not supported");
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
	protected List<ChartProperty> convertToChartProperties(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			List<ChartProperty> chartProperties = new ArrayList<ChartProperty>();

			for (Object ele : array)
			{
				ChartProperty chartProperty = convertToChartProperty(ele);

				if (chartProperty != null)
					chartProperties.add(chartProperty);
			}

			if (chartProperties.isEmpty())
				return null;

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
			Map<String, ?> map = (Map<String, ?>) obj;

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
			chartProperty.setConstraints(convertToConstraints(map.get(CHART_PROPERTY_CONSTRAINTS)));

			return chartProperty;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ ChartProperty.class.getName() + "] is not supported");
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
	 * 将对象转换为{@linkplain Constraint}集合。
	 * 
	 * @param obj
	 * @return
	 */
	protected Set<Constraint> convertToConstraints(Object obj)
	{
		if (obj == null)
			return null;
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			Set<Constraint> constraints = new HashSet<Constraint>();

			for (Map.Entry<String, ?> entry : map.entrySet())
			{
				Constraint constraint = convertToConstraint(entry.getKey(), entry.getValue());

				if (constraint != null)
					constraints.add(constraint);
			}

			return constraints;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ Constraint.class.getName() + "] set is not supported");
	}

	/**
	 * 将对象转换为{@linkplain Constraint}，不支持则返回{@code null}。
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	protected Constraint convertToConstraint(String name, Object value)
	{
		if (value == null)
			return null;
		else if (Max.class.getSimpleName().equalsIgnoreCase(name))
		{
			Number v = (Number) this.propertyTypeValueConverter.convert(PropertyType.NUMBER, value);
			return new Max(v);
		}
		else if (MaxLength.class.getSimpleName().equalsIgnoreCase(name))
		{
			Number v = (Number) this.propertyTypeValueConverter.convert(PropertyType.NUMBER, value);
			return new MaxLength(v.intValue());
		}
		else if (Min.class.getSimpleName().equalsIgnoreCase(name))
		{
			Number v = (Number) this.propertyTypeValueConverter.convert(PropertyType.NUMBER, value);
			return new Min(v);
		}
		else if (MinLength.class.getSimpleName().equalsIgnoreCase(name))
		{
			Number v = (Number) this.propertyTypeValueConverter.convert(PropertyType.NUMBER, value);
			return new MinLength(v.intValue());
		}
		else if (Required.class.getSimpleName().equalsIgnoreCase(name))
		{
			Boolean v = (Boolean) this.propertyTypeValueConverter.convert(PropertyType.BOOLEAN, value);
			return new Required(v);
		}
		else
			return null;
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
	 * 将对象转换为{@linkplain DataSigns}。
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

			List<DataSign> dataSigns = new ArrayList<DataSign>();

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

			String name = (String) map.get(DATA_SIGN_PROPERTY_NAME);
			if (name == null || name.isEmpty())
				return null;

			DataSign dataSign = createDataSign();
			dataSign.setName(name);

			dataSign.setOccurRequired(convertToBoolean(map.get(DATA_SIGN_PROPERTY_OCCUR_REQUIRED), true));
			dataSign.setOccurMultiple(convertToBoolean(map.get(DATA_SIGN_PROPERTY_OCCUR_MULTIPLE), true));
			dataSign.setNameLabel(convertToLabel(map.get(CHART_PROPERTY_NAME_LABEL)));
			dataSign.setDescLabel(convertToLabel(map.get(CHART_PROPERTY_DESC_LABEL)));

			return dataSign;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName() + "] to ["
					+ DataSign.class.getName() + "] is not supported");
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

	/**
	 * 字符串转换为{@linkplain Locale}。
	 * 
	 * @param str
	 * @return
	 */
	protected Locale stringToLocale(String str)
	{
		return Label.toLocale(str);
	}

	protected Label createLabel()
	{
		return new Label();
	}

	protected LocationIcon createLocationIcon()
	{
		return new LocationIcon();
	}

	protected ChartProperty createChartProperty()
	{
		return new ChartProperty();
	}

	protected DataSign createDataSign()
	{
		return new DataSign();
	}
}
