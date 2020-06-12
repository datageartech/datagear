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
	private String titleColor;

	/** 图例颜色 */
	private String legendColor;

	/** 坐标轴颜色 */
	private String axisColor;

	/** 坐标轴刻度线线颜色 */
	private String axisScaleLineColor;

	/** 图形条目颜色 */
	private String[] graphColors;

	/** 值域映射范围图形条目颜色 */
	private String[] graphRangeColors;

	/** 提示框主题 */
	private Theme tooltipTheme;

	/** 高亮区主题 */
	private Theme highlightTheme;

	public ChartTheme()
	{
	}

	public ChartTheme(String name, String color, String backgroundColor, String borderColor, String titleColor,
			String legendColor, String axisColor, String axisScaleLineColor, String[] graphColors,
			String[] graphRangeColors, Theme tooltipTheme, Theme highlightTheme)
	{
		this(name, color, backgroundColor, backgroundColor, borderColor, titleColor, legendColor, axisColor,
				axisScaleLineColor, graphColors, graphRangeColors, tooltipTheme, highlightTheme);
	}

	public ChartTheme(String name, String color, String backgroundColor, String actualBackgroundColor,
			String borderColor, String titleColor, String legendColor, String axisColor, String axisScaleLineColor,
			String[] graphColors, String[] graphRangeColors, Theme tooltipTheme, Theme highlightTheme)
	{
		super(name, color, backgroundColor, actualBackgroundColor, borderColor);
		this.titleColor = titleColor;
		this.legendColor = legendColor;
		this.axisColor = axisColor;
		this.axisScaleLineColor = axisScaleLineColor;
		this.graphColors = graphColors;
		this.graphRangeColors = graphRangeColors;
		this.tooltipTheme = tooltipTheme;
		this.highlightTheme = highlightTheme;
	}

	public String getTitleColor()
	{
		return titleColor;
	}

	public void setTitleColor(String titleColor)
	{
		this.titleColor = titleColor;
	}

	public String getLegendColor()
	{
		return legendColor;
	}

	public void setLegendColor(String legendColor)
	{
		this.legendColor = legendColor;
	}

	public String getAxisColor()
	{
		return axisColor;
	}

	public void setAxisColor(String axisColor)
	{
		this.axisColor = axisColor;
	}

	public String getAxisScaleLineColor()
	{
		return axisScaleLineColor;
	}

	public void setAxisScaleLineColor(String axisScaleLineColor)
	{
		this.axisScaleLineColor = axisScaleLineColor;
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

	public Theme getTooltipTheme()
	{
		return tooltipTheme;
	}

	public void setTooltipTheme(Theme tooltipTheme)
	{
		this.tooltipTheme = tooltipTheme;
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
				+ ", axisColor=" + axisColor + ", axisScaleLineColor=" + axisScaleLineColor + ", graphColors="
				+ Arrays.toString(graphColors) + ", graphRangeColors=" + Arrays.toString(graphRangeColors)
				+ ", tooltipTheme=" + tooltipTheme + ", highlightTheme=" + highlightTheme + "]";
	}
}
