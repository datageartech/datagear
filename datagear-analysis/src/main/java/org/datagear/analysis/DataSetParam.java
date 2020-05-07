/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

/**
 * 数据集参数。
 * <p>
 * 此类描述{@linkplain DataSet}获取{@linkplain DataSetResult}所需要的输入参数信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParam extends AbstractDataNameType
{
	/** 是否必须 */
	private boolean required;

	/** 参数描述 */
	private String desc;

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

	public boolean hasDesc()
	{
		return (this.desc != null && !this.desc.isEmpty());
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", required=" + required
				+ "]";
	}
}
