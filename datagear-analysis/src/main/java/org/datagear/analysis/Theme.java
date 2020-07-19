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

	/** 透明颜色值常量 */
	public static final String COLOR_TRANSPARENT = "transparent";

	/** 主题名称 */
	private String name;

	/** 前景色 */
	private String color;

	/** 背景色 */
	private String backgroundColor;

	/** 实际背景色 */
	private String actualBackgroundColor;

	/** 边框颜色 */
	private String borderColor;

	/** 边框宽度 */
	private String borderWidth = "0";

	/** 背景色至前景色的渐变跨度 */
	private int gradient = 10;

	public Theme()
	{
		super();
	}

	public Theme(String name, String color, String backgroundColor, String borderColor)
	{
		this(name, color, backgroundColor, backgroundColor, borderColor);
	}

	public Theme(String name, String color, String backgroundColor, String actualBackgroundColor, String borderColor)
	{
		super();
		this.name = name;
		this.color = color;
		this.backgroundColor = backgroundColor;
		this.setActualBackgroundColor(actualBackgroundColor);
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

	/**
	 * 获取背景色。
	 * <p>
	 * 背景色可能是透明色{@linkplain #COLOR_TRANSPARENT}。
	 * </p>
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

	/**
	 * 获取实际背景色。
	 * <p>
	 * 实际背景色不会是透明色{@linkplain #COLOR_TRANSPARENT}。
	 * </p>
	 * 
	 * @return
	 */
	public String getActualBackgroundColor()
	{
		return actualBackgroundColor;
	}

	/**
	 * 设置实际背景色。
	 * 
	 * @param actualBackgroundColor
	 * @throws IllegalArgumentException
	 *             当参数为{@linkplain #COLOR_TRANSPARENT}时
	 */
	public void setActualBackgroundColor(String actualBackgroundColor) throws IllegalArgumentException
	{
		if (COLOR_TRANSPARENT.equals(actualBackgroundColor))
			throw new IllegalArgumentException("[actualBackgroundColor] must not be '" + COLOR_TRANSPARENT + "'");

		this.actualBackgroundColor = actualBackgroundColor;
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

	public int getGradient()
	{
		return gradient;
	}

	public void setGradient(int gradient)
	{
		this.gradient = gradient;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", color=" + color + ", backgroundColor="
				+ backgroundColor + ", actualBackgroundColor=" + actualBackgroundColor + ", borderColor=" + borderColor
				+ ", borderWidth=" + borderWidth + ", gradient=" + gradient + "]";
	}
}
