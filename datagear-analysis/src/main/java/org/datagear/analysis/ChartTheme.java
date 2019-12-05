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

	/** 提示框主题 */
	private Theme tooltipTheme;

	public ChartTheme()
	{
	}

	public ChartTheme(String backgroundColor, String foregroundColor, String borderColor, String[] graphColors,
			Theme tooltipTheme)
	{
		super(backgroundColor, foregroundColor, borderColor);
		this.graphColors = graphColors;
		this.tooltipTheme = tooltipTheme;
	}

	public String[] getGraphColors()
	{
		return graphColors;
	}

	public void setGraphColors(String[] graphColors)
	{
		this.graphColors = graphColors;
	}

	public Theme getTooltipTheme()
	{
		return tooltipTheme;
	}

	public void setTooltipTheme(Theme tooltipTheme)
	{
		this.tooltipTheme = tooltipTheme;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [backgroundColor=" + getBackgroundColor() + ", foregroundColor="
				+ getForegroundColor() + ", borderColor=" + getBorderColor() + ", graphColors="
				+ Arrays.toString(this.graphColors) + ", tooltipTheme=" + tooltipTheme + "]";
	}
}
