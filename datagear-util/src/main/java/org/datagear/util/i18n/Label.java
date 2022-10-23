/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.i18n;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 标签。
 * 
 * @author datagear@163.com
 *
 */
public class Label implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_VALUE = "value";
	public static final String PROPERTY_LOCALE_VALUES = "localeValues";

	private String value = "";

	private Map<String, String> localeValues = null;

	public Label()
	{
		super();
	}

	public Label(String value)
	{
		super();
		this.value = value;
	}

	public Label(String value, Map<String, String> localeValues)
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
	 * <p>
	 * 如果没有匹配的，将返回空字符串{@code ""}。
	 * </p>
	 * 
	 * @param locale
	 * @return
	 */
	public String getValue(Locale locale)
	{
		String value = null;
		
		if(this.localeValues == null || this.localeValues.isEmpty() || locale == null)
		{
			value = this.value;
		}
		else
		{
			List<String> keys = getStringPriorityList(locale);
			for(String key : keys)
			{
				value = this.localeValues.get(key);
				
				if(value != null)
					break;
			}
		}
		
		if (value == null)
			value = this.value;
		
		if(value == null)
			value = "";
		
		return value;
	}

	/**
	 * 获取地区标签值映射表。
	 * 
	 * @return 可能返回{@code null}
	 */
	public Map<String, String> getLocaleValues()
	{
		return this.localeValues;
	}

	/**
	 * 设置地区标签值映射表。
	 * 
	 * @param localeValues
	 */
	public void setLocaleValues(Map<String, String> localeValues)
	{
		this.localeValues = localeValues;
	}
	
	public static List<String> getStringPriorityList(Locale locale)
	{
		List<String> re = null;
		
		WeakReference<List<String>> wr = LOCALE_STRING_PRIORITY_LIST.get(locale);
		re = (wr == null ? null : wr.get());
		
		if(re == null)
		{
			re = new ArrayList<String>(4);
			
			String l0 = locale.toString();
			String l1 = new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant()).toString();
			String l2 = new Locale(locale.getLanguage(), locale.getCountry()).toString();
			String l3 = new Locale(locale.getLanguage()).toString();
			
			re.add(l0);
			if(!l1.equals(l0))
				re.add(l1);
			if(!l2.equals(l1))
				re.add(l2);
			if(!l3.equals(l2))
				re.add(l3);
			
			LOCALE_STRING_PRIORITY_LIST.put(locale, new WeakReference<List<String>>(re));
		}
		
		return re;
	}
	
	private static final ConcurrentMap<Locale, WeakReference<List<String>>> LOCALE_STRING_PRIORITY_LIST = new ConcurrentHashMap<Locale, WeakReference<List<String>>>();
	
	/**
	 * 将{@linkplain Label}转换为指定{@linkplain Locale}的{@linkplain Label}。
	 * <p>
	 * 返回的{@linkplain Label#getValue()}是最匹配给定{@linkplain Locale}的值、{@linkplain Label#getLocaleValues()}则为{@code null}。
	 * </p>
	 * <p>
	 * 如果{@code label}参数为{@code null}或{@linkplain Label#getValue()}为{@code null}，
	 * 则返回一个{@linkplain Label#getValue()}为{@code ""}空字符串的对象。
	 * </p>
	 * 
	 * @param label
	 * @param locale
	 * @return
	 */
	public static Label concrete(Label label, Locale locale)
	{
		if (label == null || locale == null)
			return new Label("");

		String value = label.getValue(locale);
		
		if (value == null)
			value = "";

		return new Label(value);
	}
}
