/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Writer;

/**
 * HTML图表上下文。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartContext implements ChartContext
{
	private Writer out;

	public HtmlChartContext()
	{
	}

	public HtmlChartContext(Writer out)
	{
		super();
		this.out = out;
	}

	public Writer getOut()
	{
		return out;
	}

	public void setOut(Writer out)
	{
		this.out = out;
	}
}
