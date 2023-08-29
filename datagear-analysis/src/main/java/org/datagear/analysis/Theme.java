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

package org.datagear.analysis;

import java.io.Serializable;

/**
 * 主题。
 * 
 * @author datagear@163.com
 *
 */
public class Theme implements NameAware, Serializable
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
	
	@Override
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
