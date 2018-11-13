/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.vo;

import java.io.Serializable;

/**
 * 属性路径及其展示名称。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyPathDisplayName implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String propertyPath;

	private String displayName;

	public PropertyPathDisplayName()
	{
		super();
	}

	public PropertyPathDisplayName(String propertyPath, String displayName)
	{
		super();
		this.propertyPath = propertyPath;
		this.displayName = displayName;
	}

	public String getPropertyPath()
	{
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [propertyPath=" + propertyPath + ", displayName=" + displayName + "]";
	}
}
