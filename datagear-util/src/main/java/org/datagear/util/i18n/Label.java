/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.i18n;

import java.io.Serializable;
import java.util.List;
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
	 *            允许为{@code null}
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
			List<String> keys = getPriorityStringList(locale);
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
	
	protected List<String> getPriorityStringList(Locale locale)
	{
		return LabelUtil.getPriorityStringList(locale);
	}
}
