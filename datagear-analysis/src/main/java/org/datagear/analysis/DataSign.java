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

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;
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

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public DataSign clone(Locale locale)
	{
		DataSign target = new DataSign(this.name, this.required, this.multiple);
		LabelUtil.concrete(this, target, locale);

		return target;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", required=" + required + ", multiple="
				+ multiple + ", nameLabel=" + getNameLabel() + ", descLabel=" + getDescLabel() + "]";
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param dataSigns
	 * @param locale
	 * @return
	 */
	public static List<DataSign> clone(List<DataSign> dataSigns, Locale locale)
	{
		if (dataSigns == null)
			return null;

		List<DataSign> re = new ArrayList<DataSign>(dataSigns.size());

		for (DataSign dataSign : dataSigns)
			re.add(dataSign.clone(locale));

		return re;
	}
}
