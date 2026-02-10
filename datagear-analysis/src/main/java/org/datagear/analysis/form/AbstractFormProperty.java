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
import java.util.Map;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.LabelUtil;

/**
 * 抽象{@linkplain FormProperty}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractFormProperty extends AbstractLabeled
		implements FormProperty
{
	private static final long serialVersionUID = 1L;

	private String name;

	private String type;

	private boolean required = false;

	private boolean array = false;

	/**
	 * 默认值。
	 * <p>
	 * 注意：当{@linkplain #isArray()}为{@code true}时，此值应是元素的默认值。
	 * </p>
	 */
	private Object defaultValue = null;

	/** 扩展属性 */
	private Map<String, ?> additions = null;

	public AbstractFormProperty()
	{
		super();
	}

	public AbstractFormProperty(String name, String type)
	{
		super();
		this.name = name;
		this.type = type;
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
	public boolean isArray()
	{
		return array;
	}

	public void setArray(boolean array)
	{
		this.array = array;
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
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	@Override
	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}

	@Override
	public AbstractFormProperty toLocale(Locale locale)
	{
		AbstractFormProperty target = createEmpty();

		target.setName(this.name);
		target.setType(this.type);
		target.setRequired(this.required);
		target.setArray(this.array);
		target.setDefaultValue(this.defaultValue);
		target.setAdditions(this.additions);
		LabelUtil.concrete(this, target, locale);

		return target;
	}

	protected abstract AbstractFormProperty createEmpty();
}
