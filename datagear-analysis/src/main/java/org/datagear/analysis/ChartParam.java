/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.io.Serializable;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.Labeled;

/**
 * 图表参数。
 * <p>
 * 此类描述{@linkplain ChartPlugin}创建{@linkplain Chart}所需要的输入参数信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartParam extends AbstractLabeled implements DataNameType, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;

	/** 名称 */
	private String name;

	/** 类型 */
	private DataType type;

	/** 是否必须 */
	private boolean required;

	public ChartParam()
	{
	}

	public ChartParam(String name, DataType type, boolean required)
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

	@Override
	public DataType getType()
	{
		return type;
	}

	public void setType(DataType type)
	{
		this.type = type;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}
}
