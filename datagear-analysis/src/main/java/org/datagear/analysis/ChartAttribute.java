/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Labeled;

/**
 * 图表属性。
 * <p>
 * 此类描述{@linkplain ChartPlugin#renderChart(RenderContext, ChartDefinition)}的{@linkplain ChartDefinition#setAttrValues(java.util.Map)}支持设置的属性元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartAttribute extends AbstractLabeled implements NameTypeInputAware, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_REQUIRED = "required";
	public static final String PROPERTY_MULTIPLE = "multiple";
	public static final String PROPERTY_INPUT_TYPE = "inputType";
	public static final String PROPERTY_INPUT_PAYLOAD = "inputPayload";
	public static final String PROPERTY_GROUP = "group";

	/** 名称 */
	private String name;

	/** 类型 */
	private String type;

	/** 是否必须 */
	private boolean required;
	
	/** 是否多项 */
	private boolean multiple;

	/** 界面输入框类型 */
	private String inputType = "";

	/** 界面输入框载荷，比如：输入框为下拉选择时，定义选项内容JSON；输入概况为日期时，定义日期格式 */
	private String inputPayload = "";

	/** 所属分组 */
	private Group group = null;

	public ChartAttribute()
	{
	}

	public ChartAttribute(String name, String type, boolean required, boolean multiple)
	{
		super();
		this.name = name;
		this.type = type;
		this.required = required;
		this.multiple = multiple;
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

	@Override
	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

	/**
	 * 获取输入框类型，常用类型参考{@linkplain InputType}。
	 * 
	 * @return
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
	public String getInputPayload()
	{
		return inputPayload;
	}

	public void setInputPayload(String inputPayload)
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

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public ChartAttribute clone(Locale locale)
	{
		ChartAttribute target = new ChartAttribute(this.name, this.type, this.required, this.multiple);
		target.setInputType(this.inputType);
		target.setInputPayload(this.inputPayload);
		LabelUtil.concrete(this, target, locale);

		if (this.group != null)
			target.setGroup(this.group.clone(locale));

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
	 * @param chartAttributes
	 * @param locale
	 * @return
	 */
	public static List<ChartAttribute> clone(List<ChartAttribute> chartAttributes, Locale locale)
	{
		if (chartAttributes == null)
			return null;

		List<ChartAttribute> re = new ArrayList<ChartAttribute>(chartAttributes.size());

		for (ChartAttribute chartAttribute : chartAttributes)
			re.add(chartAttribute.clone(locale));

		return re;
	}

	/**
	 * {@linkplain ChartAttribute#getType()}枚举。
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
	 * 常用的{@linkplain ChartAttribute#getInputType()}枚举。
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

		/** 日期 */
		public static final String DATE = "date";

		/** 时间 */
		public static final String TIME = "time";

		/** 日期时间 */
		public static final String DATETIME = "datetime";

		/** 颜色 */
		public static final String COLOR = "color";
	}
}
