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
 * 数据标记。
 * <p>
 * 在{@linkplain Chart}中通过{@linkplain Chart#setDataSetBinds(DataSetBind[])}关联绑定数据集时，
 * 使用{@linkplain ChartPlugin}提供的{@linkplain ChartPlugin#getDataSigns()}标记，
 * 使用此类标记{@linkplain DataSet}、{@linkplain DataSetField}，{@linkplain ChartPlugin}则依据它们构建图表数据，进行图表绘制。
 * </p>
 * <p>
 * 数据标记分为两类：数据集标记、字段标记，通过{@linkplain #getTarget()}区分。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSign extends AbstractLabeled implements AdditionsAware, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TARGET = "target";
	public static final String PROPERTY_REQUIRED = "required";
	public static final String PROPERTY_MULTIPLE = "multiple";
	public static final String PROPERTY_CHILDREN = "children";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	/**
	 * 标记目标：字段
	 */
	public static final String TARGET_FIELD = "FIELD";

	/**
	 * 标记目标：数据集
	 */
	public static final String TARGET_DATASET = "DATASET";

	/** 名称 */
	private String name;

	/**
	 * 标记目标。
	 * <p>
	 * 如果值为{@linkplain #TARGET_DATASET}，应仅用于标记{@linkplain DataSetBind#getDataSet()}；
	 * 如果值为{@linkplain #TARGET_FIELD}，应仅用于标记{@linkplain DataSetBind#getDataSet()}所包含的{@linkplain DataSetField}。
	 * </p>
	 * <p>
	 * 注意：默认值应设为{@linkplain #TARGET_FIELD}，以兼容旧版逻辑。
	 * </p>
	 */
	private String target = TARGET_FIELD;

	/** 数据集是否必须有此标记 */
	private boolean required;

	/** 数据集是否可有多个此标记 */
	private boolean multiple;

	/**
	 * 当{@linkplain #target}是{@linkplain #TARGET_DATASET}时的子{@linkplain DataSign}（可选），
	 * 它们应只标记于已经标记过此{@linkplain DataSign}的{@linkplain DataSetBind#getDataSet()}所包含的{@linkplain DataSetField}。
	 */
	private List<DataSign> children = null;

	/** 附加属性 */
	private Map<String, ?> additions = null;

	public DataSign()
	{
		super();
	}

	public DataSign(String name, boolean required, boolean multiple)
	{
		this(name, TARGET_FIELD, required, multiple);
	}

	public DataSign(String name, String target, boolean required, boolean multiple)
	{
		super();
		this.name = name;
		this.target = target;
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

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
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

	public List<DataSign> getChildren()
	{
		return children;
	}

	public void setChildren(List<DataSign> children)
	{
		this.children = children;
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
	public DataSign clone(Locale locale)
	{
		DataSign re = new DataSign(this.name, this.target, this.required, this.multiple);
		re.setAdditions(this.additions);
		LabelUtil.concrete(this, re, locale);

		if (this.children != null)
		{
			List<DataSign> reChildren = new ArrayList<>(this.children.size());

			for (DataSign dataSign : this.children)
			{
				reChildren.add(dataSign.clone(locale));
			}

			re.setChildren(reChildren);
		}

		return re;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", required=" + required + ", multiple="
				+ multiple + ", nameLabel=" + getNameLabel() + ", descLabel=" + getDescLabel() + ", additions="
				+ additions + "]";
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
