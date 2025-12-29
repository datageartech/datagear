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

package org.datagear.analysis.pluginattr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.analysis.NameTypeInputAware;
import org.datagear.util.i18n.LabelUtil;

/**
 * 输入项图表插件属性。
 * <p>
 * 一个输入项描述插件属性值对象中的一个可输入的基本属性值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginInputAttribute extends AbstractChartPluginAttribute implements NameTypeInputAware
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_INPUT_TYPE = "inputType";
	public static final String PROPERTY_INPUT_PAYLOAD = "inputPayload";

	/** 类型 */
	private String type;

	/** 输入框类型 */
	private String inputType = null;

	/** 输入框载荷 */
	private Object inputPayload = null;
	
	public ChartPluginInputAttribute()
	{
		super();
	}

	public ChartPluginInputAttribute(String name, String type)
	{
		super(name);
		this.type = type;
	}

	/**
	 * 获取数据类型，参考{@linkplain DataType}。
	 */
	@Override
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * 获取输入框类型，常用类型参考{@linkplain InputType}。
	 * 
	 * @return 可能为{@code null}
	 */
	@Override
	public String getInputType()
	{
		return inputType;
	}

	public void setInputType(String inputType)
	{
		this.inputType = inputType;
	}

	@Override
	public Object getInputPayload()
	{
		return inputPayload;
	}

	public void setInputPayload(Object inputPayload)
	{
		this.inputPayload = inputPayload;
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public ChartPluginInputAttribute clone(Locale locale)
	{
		ChartPluginInputAttribute target = new ChartPluginInputAttribute(getName(), this.type);
		target.setRequired(isRequired());
		target.setArray(isArray());
		target.setInputType(this.inputType);
		target.setInputPayload(this.inputPayload);
		target.setAdditions(getAdditions());
		LabelUtil.concrete(this, target, locale);

		return target;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", required=" + isRequired() + ", array="
				+ isArray() + ", type=" + type + ", inputType=" + inputType + "]";
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param attributes
	 * @param locale
	 * @return
	 */
	public static List<ChartPluginInputAttribute> clone(List<ChartPluginInputAttribute> attributes,
			Locale locale)
	{
		if (attributes == null)
			return null;

		if (attributes.isEmpty())
			return Collections.emptyList();

		List<ChartPluginInputAttribute> re = new ArrayList<ChartPluginInputAttribute>(attributes.size());

		for (ChartPluginInputAttribute attribute : attributes)
			re.add(attribute.clone(locale));

		return re;
	}

	/**
	 * {@linkplain ChartPluginInputAttribute#getType()}枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DataType
	{
		/** 字符串 */
		public static final String STRING = "STRING";

		/** 布尔值 */
		public static final String BOOLEAN = "BOOLEAN";

		/** 数值 */
		public static final String NUMBER = "NUMBER";
	}
	
	/**
	 * 常用的{@linkplain ChartPluginInputAttribute#getInputType()}枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class InputType
	{
		/** 文本框 */
		public static final String TEXT = "text";

		/** 下拉框 */
		public static final String SELECT = "select";

		/** 单选框 */
		public static final String RADIO = "radio";

		/** 复选框 */
		public static final String CHECKBOX = "checkbox";

		/** 文本域 */
		public static final String TEXTAREA = "textarea";

		/** 颜色 */
		public static final String COLOR = "color";
	}
}