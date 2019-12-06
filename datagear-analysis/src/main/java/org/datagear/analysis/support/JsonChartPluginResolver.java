/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.Icon;
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

	private ConcurrentMap<String, Locale> _localeCache = new ConcurrentHashMap<String, Locale>();

	public JsonChartPluginResolver()
	{
		super();
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
			return new Label((String) obj);
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) obj;

			Label label = new Label();
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
			return new LocationIcon((String) obj);
		}
		else if (obj instanceof Map<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, String>) obj;

			String location = (String) map.get(LOCATION_ICON_LOCATION);

			if (location == null)
				return null;

			return new LocationIcon(location);
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
		if (obj == null)
			return null;
		else if (obj instanceof RenderStyle)
			return (RenderStyle) obj;
		else if (obj instanceof String)
		{
			String strRenderStyle = (String)obj;
			RenderStyle[] renderStyles = RenderStyle.values();
			
			for(RenderStyle renderStyle : renderStyles)
			{
				if (renderStyle.name().equalsIgnoreCase(strRenderStyle))
					return renderStyle;
			}

			return null;
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + obj.getClass().getName()
					+ "] to [" + Icon.class.getName() + "] is not supported");
	}
}
