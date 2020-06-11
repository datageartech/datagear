/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Serializable;

/**
 * 主题。
 * 
 * @author datagear@163.com
 *
 */
public class Theme implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 主题名称 */
	private String name;

	/** 前景色 */
	private String color;

	/** 背景色 */
	private String backgroundColor;

	/** 边框颜色 */
	private String borderColor;

	/** 边框宽度 */
	private String borderWidth = "0";

	public Theme()
	{
		super();
	}

	public Theme(String name, String color, String backgroundColor, String borderColor)
	{
		super();
		this.name = name;
		this.color = color;
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

	public String getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public String getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(String borderColor)
	{
		this.borderColor = borderColor;
	}

	public String getBorderWidth()
	{
		return borderWidth;
	}

	public void setBorderWidth(String borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name= " + this.name + ", color=" + color + ", backgroundColor="
				+ backgroundColor + ", borderColor=" + borderColor + ", borderWidth=" + borderWidth + "]";
	}
}
