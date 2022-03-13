/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	private String borderColor = "";

	/** 边框宽度 */
	private String borderWidth = "";

	/** 字体尺寸 */
	private String fontSize = "";

	public Theme()
	{
		super();
	}

	public Theme(String name, String color, String backgroundColor)
	{
		super();
		this.name = name;
		this.color = color;
		this.backgroundColor = backgroundColor;
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

	/**
	 * 获取背景色。
	 * 
	 * @return
	 */
	public String getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public boolean hasBorderColor()
	{
		return (this.borderColor != null && !this.borderColor.isEmpty());
	}

	public String getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(String borderColor)
	{
		this.borderColor = borderColor;
	}

	public boolean hasBorderWidth()
	{
		return (this.borderWidth != null && !this.borderWidth.isEmpty());
	}

	public String getBorderWidth()
	{
		return borderWidth;
	}

	public void setBorderWidth(String borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	public boolean hasFontSize()
	{
		return (this.fontSize != null && !this.fontSize.isEmpty());
	}

	public String getFontSize()
	{
		return fontSize;
	}

	public void setFontSize(String fontSize)
	{
		this.fontSize = fontSize;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", color=" + color + ", backgroundColor="
				+ backgroundColor + ", borderColor="
				+ borderColor + ", borderWidth=" + borderWidth + ", fontSize=" + fontSize + "]";
	}
}
