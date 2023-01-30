/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.management.service.impl;

import java.io.Serializable;

/**
 * 枚举值标签。
 * 
 * @author datagear@163.com
 *
 */
public class EnumValueLabel<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 枚举值 */
	private T value;

	/** 标签 */
	private String label;

	public EnumValueLabel()
	{
		super();
	}

	public EnumValueLabel(T value, String label)
	{
		super();
		this.value = value;
		this.label = label;
	}

	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}
}
