/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

/**
 * 数据集参数。
 * <p>
 * 此类描述{@linkplain DataSetFactory}创建{@linkplain DataSet}所需要的输入参数信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParam extends DataNameAndType
{
	/** 是否必须 */
	private boolean required;

	/** 默认值 */
	private Object defaultValue;

	public DataSetParam()
	{
		super();
	}

	public DataSetParam(String name, DataType type, boolean required)
	{
		super(name, type);
		this.required = required;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", required=" + required
				+ ", defaultValue=" + defaultValue + "]";
	}
}
