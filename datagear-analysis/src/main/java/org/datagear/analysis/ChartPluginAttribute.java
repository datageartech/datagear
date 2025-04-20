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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Labeled;

/**
 * 图表插件属性。
 * <p>
 * 此类描述{@linkplain ChartPlugin#renderChart(ChartDefinition, RenderContext)}的{@linkplain ChartDefinition#setAttrValues(java.util.Map)}支持设置的属性元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginAttribute extends AbstractLabeled implements NameTypeInputAware, AdditionsAware, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_REQUIRED = "required";
	public static final String PROPERTY_INPUT_TYPE = "inputType";
	public static final String PROPERTY_INPUT_PAYLOAD = "inputPayload";
	public static final String PROPERTY_GROUP = "group";
	public static final String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	/** 名称 */
	private String name;

	/** 类型 */
	private String type;

	/** 是否必须 */
	private boolean required;
	
	/** 输入框类型 */
	private String inputType = null;

	/** 输入框载荷 */
	private Object inputPayload = null;
	
	/** 所属分组 */
	private Group group = null;

	/** 扩展属性 */
	private Map<String, ?> additions = null;

	public ChartPluginAttribute()
	{
	}

	public ChartPluginAttribute(String name, String type, boolean required)
	{
		super();
		this.name = name;
		this.type = type;
		this.required = required;
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

	@Override
	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
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
	 * 获取所属{@linkplain Group}。
	 * 
	 * @return 为{@code null}表示无分组
	 */
	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	@Override
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public ChartPluginAttribute clone(Locale locale)
	{
		ChartPluginAttribute target = new ChartPluginAttribute(this.name, this.type, this.required);
		target.setInputType(this.inputType);
		target.setInputPayload(this.inputPayload);
		target.setGroup(this.group != null ? this.group.clone(locale) : null);
		target.setAdditions(this.additions);
		LabelUtil.concrete(this, target, locale);

		return target;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param chartPluginAttributes
	 * @param locale
	 * @return
	 */
	public static List<ChartPluginAttribute> clone(List<ChartPluginAttribute> chartPluginAttributes, Locale locale)
	{
		if (chartPluginAttributes == null)
			return null;

		List<ChartPluginAttribute> re = new ArrayList<ChartPluginAttribute>(chartPluginAttributes.size());

		for (ChartPluginAttribute chartPluginAttribute : chartPluginAttributes)
			re.add(chartPluginAttribute.clone(locale));

		return re;
	}

	/**
	 * {@linkplain ChartPluginAttribute#getType()}枚举。
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
	 * 常用的{@linkplain ChartPluginAttribute#getInputType()}枚举。
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
