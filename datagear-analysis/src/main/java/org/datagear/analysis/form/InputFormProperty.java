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

package org.datagear.analysis.form;

import java.util.Locale;

import org.datagear.analysis.NameTypeInputAware;

/**
 * 输入框表单属性。
 * <p>
 * 一个输入框属性描述{@linkplain Form}中的一个基本型的UI交互操作元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class InputFormProperty extends AbstractFormProperty implements NameTypeInputAware
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_INPUT_TYPE = "inputType";
	public static final String PROPERTY_INPUT_PAYLOAD = "inputPayload";
	public static final String PROPERTY_DEFAULT_VALUE = DefaultValueAware.PROPERTY_DEFAULT_VALUE;
	public static final String PROPERTY_GROUP = "group";

	/** 输入框类型 */
	private String inputType = null;

	/** 输入框载荷 */
	private Object inputPayload = null;

	/** 默认值 */
	private Object defaultValue = null;

	public InputFormProperty()
	{
		super();
	}

	public InputFormProperty(String name, String type)
	{
		super(name, type);
	}

	public InputFormProperty(String name, String type, boolean required)
	{
		super(name, type);
		setRequired(required);
	}

	/**
	 * 获取名称，不应为空。
	 */
	@Override
	public String getName()
	{
		return super.getName();
	}

	/**
	 * 设置名称，不应为空。
	 */
	@Override
	public void setName(String name)
	{
		super.setName(name);
	}

	/**
	 * 获取输入框类型，常用类型参考{@linkplain PropertyInputType}。
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

	@Override
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public InputFormProperty toLocale(Locale locale)
	{
		InputFormProperty target = (InputFormProperty) super.toLocale(locale);

		target.setInputType(this.inputType);
		target.setInputPayload(this.inputPayload);
		target.setDefaultValue(this.defaultValue);

		return target;
	}

	@Override
	protected InputFormProperty createEmpty()
	{
		return new InputFormProperty();
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", required=" + isRequired()
				+ ", array=" + isArray() + ", inputType=" + inputType + ", defaultValue=" + defaultValue + "]";
	}
}