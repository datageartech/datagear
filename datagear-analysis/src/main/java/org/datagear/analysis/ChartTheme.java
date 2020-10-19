/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 图表主题。
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

	/** 标题颜色 */
	private String titleColor = "";

	/** 图例颜色 */
	private String legendColor = "";

	/** 图形条目颜色 */
	private String[] graphColors;

	/** 值域映射范围图形条目颜色 */
	private String[] graphRangeColors;

	/** 提示框主题 */
	private Theme tooltipTheme = null;

	/** 高亮区主题 */
	private Theme highlightTheme = null;

	public ChartTheme()
	{
	}

	public ChartTheme(String name, String color, String backgroundColor, String[] graphColors,
			String[] graphRangeColors)
	{
		super(name, color, backgroundColor);
		this.graphColors = graphColors;
		this.graphRangeColors = graphRangeColors;
	}

	public ChartTheme(String name, String color, String backgroundColor, String actualBackgroundColor,
			String[] graphColors, String[] graphRangeColors)
	{
		super(name, color, backgroundColor, actualBackgroundColor);
		this.graphColors = graphColors;
		this.graphRangeColors = graphRangeColors;
	}

	public boolean hasTitleColor()
	{
		return (this.titleColor != null && !this.titleColor.isEmpty());
	}

	public String getTitleColor()
	{
		return titleColor;
	}

	public void setTitleColor(String titleColor)
	{
		this.titleColor = titleColor;
	}

	public boolean hasLegendColor()
	{
		return (this.legendColor != null && !this.legendColor.isEmpty());
	}

	public String getLegendColor()
	{
		return legendColor;
	}

	public void setLegendColor(String legendColor)
	{
		this.legendColor = legendColor;
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
		return getClass().getSimpleName() + " [color=" + getColor() + ", backgroundColor=" + getBackgroundColor()
				+ ", borderColor=" + getBorderColor() + ", titleColor=" + titleColor + ", legendColor=" + legendColor
				+ ", graphColors=" + Arrays.toString(graphColors) + ", graphRangeColors="
				+ Arrays.toString(graphRangeColors) + ", tooltipTheme=" + tooltipTheme + ", highlightTheme="
				+ highlightTheme + "]";
	}
}
