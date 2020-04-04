/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.io.Serializable;

import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.Labeled;

/**
 * 图表插件类别。
 * 
 * @author datagear@163.com
 *
 */
public class Category extends AbstractLabeled implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_ORDER = "order";

	private String name;

	private int order = 0;

	public Category()
	{
		super();
	}

	public Category(String name)
	{
		super();
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", nameLabel=" + getNameLabel() + ", descLabel="
				+ getDescLabel() + ", order=" + order + "]";
	}
}
