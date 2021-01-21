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
import org.datagear.util.i18n.Labeled;

/**
 * 数据标记。
 * <p>
 * {@linkplain ChartPlugin}使用此类标记{@linkplain DataSet}产生的数据，并依此进行图表绘制。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSign extends AbstractLabeled implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_REQUIRED = "required";
	public static final String PROPERTY_MULTIPLE = "multiple";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;

	/** 名称 */
	private String name;

	/** 数据集是否必须有此标记 */
	private boolean required;

	/** 数据集是否可有多个此标记 */
	private boolean multiple;

	public DataSign()
	{
		super();
	}

	public DataSign(String name, boolean required, boolean multiple)
	{
		super();
		this.name = name;
		this.required = required;
		this.multiple = multiple;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", required=" + required + ", multiple="
				+ multiple + ", nameLabel=" + getNameLabel() + ", descLabel=" + getDescLabel() + "]";
	}

	public static List<DataSign> toDataSigns(List<String> labelValues, Locale locale)
	{
		List<DataSign> dataSigns = new ArrayList<DataSign>(labelValues.size());

		return dataSigns;
	}
}
