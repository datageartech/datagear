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

package org.datagear.analysis;

import java.util.Arrays;

/**
 * 图表主题。
 * <p>
 * {@linkplain #getHighlightTheme()}不是必填的，
 * 它们可以由展现界面根据{@linkplain #getColor()}、{@linkplain #getActualBackgroundColor()}自动生成。
 * </p>
 * <p>
 * 此类可为在看板内绘制统一主题的多个图表提供支持。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartTheme extends Theme
{
	private static final long serialVersionUID = 1L;

	/** 透明颜色值常量 */
	public static final String COLOR_TRANSPARENT = "transparent";

	/** 实际背景色 */
	private String actualBackgroundColor;

	/** 图形条目颜色 */
	private String[] graphColors;

	/** 值域映射范围图形条目颜色 */
	private String[] graphRangeColors;

	/** 高亮/选中等元素主题 */
	private Theme highlightTheme = null;

	public ChartTheme()
	{
	}

	public ChartTheme(String color, String backgroundColor, String actualBackgroundColor,
			String[] graphColors, String[] graphRangeColors)
	{
		super(color, backgroundColor);
		this.setActualBackgroundColor(actualBackgroundColor);
		this.graphColors = graphColors;
		this.graphRangeColors = graphRangeColors;
	}

	@Override
	public void setBackgroundColor(String backgroundColor)
	{
		super.setBackgroundColor(backgroundColor);

		if (!COLOR_TRANSPARENT.equalsIgnoreCase(actualBackgroundColor))
			this.actualBackgroundColor = backgroundColor;
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
	 * @throws IllegalArgumentException 当参数为{@linkplain #COLOR_TRANSPARENT}时
	 */
	public void setActualBackgroundColor(String actualBackgroundColor) throws IllegalArgumentException
	{
		if (COLOR_TRANSPARENT.equalsIgnoreCase(actualBackgroundColor))
			throw new IllegalArgumentException("[actualBackgroundColor] must not be '" + COLOR_TRANSPARENT + "'");

		this.actualBackgroundColor = actualBackgroundColor;
	}

	public String[] getGraphColors()
	{
		return graphColors;
	}

	public void setGraphColors(String[] graphColors)
	{
		this.graphColors = graphColors;
	}

	public String[] getGraphRangeColors()
	{
		return graphRangeColors;
	}

	public void setGraphRangeColors(String[] graphRangeColors)
	{
		this.graphRangeColors = graphRangeColors;
	}

	public boolean hasHighlightTheme()
	{
		return (this.highlightTheme != null);
	}

	public Theme getHighlightTheme()
	{
		return highlightTheme;
	}

	public void setHighlightTheme(Theme highlightTheme)
	{
		this.highlightTheme = highlightTheme;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [color=" + getColor() + ", backgroundColor="
				+ getBackgroundColor() + ", fontSize=" + getFontSize() + ", actualBackgroundColor="
				+ actualBackgroundColor + ", graphColors=" + Arrays.toString(graphColors) + ", graphRangeColors="
				+ Arrays.toString(graphRangeColors) + ", highlightTheme=" + highlightTheme + "]";
	}
}
