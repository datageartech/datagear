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

	/** 环境主要颜色 */
	private String envMajorColor;

	/** 环境次要颜色 */
	private String envMinorColor;

	/** 环境最不重要的颜色 */
	private String envLeastColor;

	/** 环境高亮颜色 */
	private String envHighlightColor;

	/** 提示框主题 */
	private Theme tooltipTheme;

	public ChartTheme()
	{
	}

	public ChartTheme(String backgroundColor, String foregroundColor, String borderColor, String envMajorColor,
			String envMinorColor, String envLeastColor, String envHighlightColor, String[] graphColors,
			Theme tooltipTheme)
	{
		super(backgroundColor, foregroundColor, borderColor);
		this.graphColors = graphColors;
		this.envMajorColor = envMajorColor;
		this.envMinorColor = envMinorColor;
		this.envLeastColor = envLeastColor;
		this.envHighlightColor = envHighlightColor;
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

	public String getEnvMajorColor()
	{
		return envMajorColor;
	}

	public void setEnvMajorColor(String envMajorColor)
	{
		this.envMajorColor = envMajorColor;
	}

	public String getEnvMinorColor()
	{
		return envMinorColor;
	}

	public void setEnvMinorColor(String envMinorColor)
	{
		this.envMinorColor = envMinorColor;
	}

	public String getEnvLeastColor()
	{
		return envLeastColor;
	}

	public void setEnvLeastColor(String envLeastColor)
	{
		this.envLeastColor = envLeastColor;
	}

	public String getEnvHighlightColor()
	{
		return envHighlightColor;
	}

	public void setEnvHighlightColor(String envHighlightColor)
	{
		this.envHighlightColor = envHighlightColor;
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
		return getClass().getSimpleName() + " [graphColors=" + Arrays.toString(graphColors) + ", envMajorColor="
				+ envMajorColor + ", envMinorColor=" + envMinorColor + ", envLeastColor=" + envLeastColor
				+ ", tooltipTheme=" + tooltipTheme + "]";
	}
}
