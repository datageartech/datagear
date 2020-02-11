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

	/** 图形颜色 */
	private String[] graphColors;

	/** 二级前景颜色 */
	private String colorSecond;

	/** 三级前景颜色 */
	private String colorThird;

	/** 四级前景颜色 */
	private String colorFourth;

	/** 提示框主题 */
	private Theme tooltipTheme;

	/** 高亮区主题 */
	private Theme highlightTheme;

	public ChartTheme()
	{
	}

	public ChartTheme(String color, String backgroundColor, String borderColor, String[] graphColors,
			String colorSecond,
			String colorThird, String colorFourth, Theme tooltipTheme, Theme highlightTheme)
	{
		super(color, backgroundColor, borderColor);
		this.graphColors = graphColors;
		this.colorSecond = colorSecond;
		this.colorThird = colorThird;
		this.colorFourth = colorFourth;
		this.tooltipTheme = tooltipTheme;
		this.highlightTheme = highlightTheme;
	}

	public String[] getGraphColors()
	{
		return graphColors;
	}

	public void setGraphColors(String[] graphColors)
	{
		this.graphColors = graphColors;
	}

	public String getColorSecond()
	{
		return colorSecond;
	}

	public void setColorSecond(String colorSecond)
	{
		this.colorSecond = colorSecond;
	}

	public String getColorThird()
	{
		return colorThird;
	}

	public void setColorThird(String colorThird)
	{
		this.colorThird = colorThird;
	}

	public String getColorFourth()
	{
		return colorFourth;
	}

	public void setColorFourth(String colorFourth)
	{
		this.colorFourth = colorFourth;
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
				+ ", borderColor=" + getBorderColor() + ", graphColors=" + Arrays.toString(graphColors)
				+ ", colorSecond=" + colorSecond + ", colorThird=" + colorThird + ", colorFourth=" + colorFourth
				+ ", tooltipTheme=" + tooltipTheme + ", highlightTheme=" + highlightTheme + "]";
	}
}
