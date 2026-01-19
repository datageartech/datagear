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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AdditionsAware;
import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.Labeled;

/**
 * 表单。
 * 
 * @author datagear@163.com
 *
 */
public class Form extends AbstractLabeled
		implements GroupFormProperties, Labeled, AdditionsAware, DefaultValueAware, Serializable
{
	private static final long serialVersionUID = 1L;

	private List<FormProperty> properties = Collections.emptyList();

	private List<FormPropertyGroup> groups = null;

	private Map<String, ?> additions = null;

	private Object defaultValue = null;

	public Form()
	{
		super();
	}

	public Form(List<FormProperty> properties)
	{
		super();
		this.properties = properties;
	}

	public Form(Label nameLabel, List<FormProperty> properties)
	{
		super();
		super.setNameLabel(nameLabel);
		this.properties = properties;
	}

	public Form(List<FormProperty> properties, List<FormPropertyGroup> groups)
	{
		super();
		this.properties = properties;
		this.groups = groups;
	}

	public Form(Label nameLabel, List<FormProperty> properties, List<FormPropertyGroup> groups)
	{
		super();
		super.setNameLabel(nameLabel);
		this.properties = properties;
		this.groups = groups;
	}

	@Override
	public List<FormProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<FormProperty> properties)
	{
		this.properties = properties;
	}

	@Override
	public List<FormPropertyGroup> getGroups()
	{
		return groups;
	}

	public void setGroups(List<FormPropertyGroup> groups)
	{
		this.groups = groups;
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
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}
}
