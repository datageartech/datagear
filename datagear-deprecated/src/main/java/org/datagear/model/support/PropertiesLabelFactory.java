/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.datagear.model.Label;

/**
 * 属性资源标签工厂。
 * <p>
 * <i>属性资源标签工厂</i>从{@linkplain Properties 属性映射表}中加载和初始化{@linkplain Label 标签}信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertiesLabelFactory implements LabelFactory
{
	private Properties properties;

	private Map<Locale, Properties> localeProperties;

	private Map<String, Label> labels;

	public PropertiesLabelFactory()
	{
		super();
	}

	public PropertiesLabelFactory(Properties properties)
	{
		super();
		this.properties = properties;
	}

	public PropertiesLabelFactory(Properties properties, Map<Locale, Properties> localeProperties)
	{
		super();
		this.properties = properties;
		this.localeProperties = localeProperties;
	}

	public Properties getProperties()
	{
		return properties;
	}

	public void setProperties(Properties properties)
	{
		this.properties = properties;
	}

	public Map<Locale, Properties> getLocaleProperties()
	{
		return localeProperties;
	}

	public void setLocaleProperties(Map<Locale, Properties> localeProperties)
	{
		this.localeProperties = localeProperties;
	}

	/**
	 * 初始化。
	 */
	public void init()
	{
		Map<String, Label> labels = resolveProperties(this.properties);

		if (this.localeProperties != null)
			resolveLocaleProperties(labels, this.localeProperties);

		this.labels = labels;
	}

	@Override
	public Label getLabel(String key)
	{
		return this.labels == null ? null : this.labels.get(key);
	}

	protected static Map<String, Label> resolveProperties(Properties properties)
	{
		Map<String, Label> labels = new HashMap<String, Label>();

		Set<String> keys = properties.stringPropertyNames();

		for (String key : keys)
		{
			String value = properties.getProperty(key);

			labels.put(key, new Label(value));
		}

		return labels;
	}

	protected void resolveLocaleProperties(Map<String, Label> labels, Map<Locale, Properties> localeProperties)
	{
		Map<String, Map<Locale, String>> keyLocaleValues = new HashMap<String, Map<Locale, String>>();

		for (Locale locale : localeProperties.keySet())
		{
			Properties properties = localeProperties.get(locale);

			for (String key : properties.stringPropertyNames())
			{
				String value = properties.getProperty(key);

				Map<Locale, String> localeValues = keyLocaleValues.get(key);
				if (localeValues == null)
				{
					localeValues = new HashMap<Locale, String>();
					keyLocaleValues.put(key, localeValues);
				}

				localeValues.put(locale, value);
			}
		}

		for (String key : labels.keySet())
		{
			Label label = labels.get(key);

			Map<Locale, String> localeValues = keyLocaleValues.get(key);

			if (localeValues != null)
				label.setLocaleValues(localeValues);
		}
	}
}
