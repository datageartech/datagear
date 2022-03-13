/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 图表主题。
 * <p>
 * {@linkplain #getTitleTheme()}、{@linkplain #getLegendTheme()}、{@linkplain #getTooltipTheme()}、{@linkplain #getHighlightTheme()}不是必填的，
 * 它们可以由展现界面根据{@linkplain #getColor()}、{@linkplain #getActualBackgroundColor()}配合{@linkplain #getGradient()}自动生成。
 * </p>
 * <p>
 * 此类可为在看板内绘制统一主题的多个图表提供支持。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartTheme extends Theme implements Serializable
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

	/** 背景色至前景色的渐变跨度 */
	private int gradient = 10;

	/** 标题主题 */
	private Theme titleTheme = null;

	/** 图例主题 */
	private Theme legendTheme = null;

	/** 提示框主题 */
	private Theme tooltipTheme = null;

	/** 高亮区主题 */
	private Theme highlightTheme = null;

	public ChartTheme()
	{
	}

	public ChartTheme(String name, String color, String backgroundColor, String actualBackgroundColor,
			String[] graphColors, String[] graphRangeColors)
	{
		super(name, color, backgroundColor);
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

	public int getGradient()
	{
		return gradient;
	}

	public void setGradient(int gradient)
	{
		this.gradient = gradient;
	}

	public boolean hasTitleTheme()
	{
		return (this.titleTheme != null);
	}

	public Theme getTitleTheme()
	{
		return titleTheme;
	}

	public void setTitleTheme(Theme titleTheme)
	{
		this.titleTheme = titleTheme;
	}

	public boolean hasLegendTheme()
	{
		return (this.legendTheme != null);
	}

	public Theme getLegendTheme()
	{
		return legendTheme;
	}

	public void setLegendTheme(Theme legendTheme)
	{
		this.legendTheme = legendTheme;
	}

	public boolean hasTooltipTheme()
	{
		return (this.tooltipTheme != null);
	}

	public Theme getTooltipTheme()
	{
		return tooltipTheme;
	}

	public void setTooltipTheme(Theme tooltipTheme)
	{
		this.tooltipTheme = tooltipTheme;
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
		return getClass().getSimpleName() + " [name=" + getName() + ", color=" + getColor() + ", backgroundColor="
				+ getBackgroundColor() + ", borderColor=" + getBorderColor() + ", borderWidth="
				+ getBorderWidth() + ", fontSize=" + getFontSize() + ", actualBackgroundColor="
				+ actualBackgroundColor + ", graphColors=" + Arrays.toString(graphColors) + ", graphRangeColors="
				+ Arrays.toString(graphRangeColors) + ", gradient=" + gradient + ", titleTheme=" + titleTheme
				+ ", legendTheme=" + legendTheme + ", tooltipTheme=" + tooltipTheme + ", highlightTheme="
				+ highlightTheme + "]";
	}
}
