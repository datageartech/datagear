/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.vo;

import java.io.Serializable;

/**
 * 属性路径及其名字标签信息。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyPathNameLabel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String propertyPath;

	private String nameLabel;

	public PropertyPathNameLabel()
	{
		super();
	}

	public PropertyPathNameLabel(String propertyPath, String nameLabel)
	{
		super();
		this.propertyPath = propertyPath;
		this.nameLabel = nameLabel;
	}

	public String getPropertyPath()
	{
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}

	public String getNameLabel()
	{
		return nameLabel;
	}

	public void setNameLabel(String nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [propertyPath=" + propertyPath + ", nameLabel=" + nameLabel + "]";
	}
}
