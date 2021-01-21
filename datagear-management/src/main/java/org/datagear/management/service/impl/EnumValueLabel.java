/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
