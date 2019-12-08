/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util.i18n;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * 标签。
 * 
 * @author datagear@163.com
 *
 */
public class Label implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String value;

	private Map<Locale, String> localeValues;

	public Label()
	{
		super();
	}

	public Label(String value)
	{
		super();
		this.value = value;
	}

	public Label(String value, Map<Locale, String> localeValues)
	{
		super();
		this.value = value;
		this.localeValues = localeValues;
	}

	/**
	 * 获取标签默认值。
	 * 
	 * @return
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * 设置标签默认值。
	 * 
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * 获取指定{@linkplain Locale}的标签值。
	 * 
	 * @param locale
	 * @return
	 */
	public String getValue(Locale locale)
	{
		String value = (this.localeValues == null ? null : this.localeValues.get(locale));

		if (value == null)
			value = this.value;

		return value;
	}

	/**
	 * 获取地区标签值映射表。
	 * 
	 * @return
	 */
	public Map<Locale, String> getLocaleValues()
	{
		return this.localeValues;
	}

	/**
	 * 设置地区标签值映射表。
	 * 
	 * @param localeValues
	 */
	public void setLocaleValues(Map<Locale, String> localeValues)
	{
		this.localeValues = localeValues;
	}

	/**
	 * 字符串转换为{@linkplain Locale}。
	 * 
	 * @param locale
	 * @return
	 */
	public static Locale toLocale(String locale)
	{
		if (locale == null)
			return null;

		String[] strs = locale.split("_");

		if (strs.length == 0)
			return null;
		else if (strs.length == 1)
			return new Locale(strs[0]);
		else if (strs.length == 2)
			return new Locale(strs[0], strs[1]);
		else
			return new Locale(strs[0], strs[1], strs[2]);
	}
}
