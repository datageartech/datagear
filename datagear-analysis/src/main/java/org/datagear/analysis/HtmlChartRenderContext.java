/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.io.Writer;

/**
 * HTML图表渲染上下文。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlChartRenderContext extends ChartRenderContext
{
	/**
	 * 获取用于输出{@linkplain Chart}的输出流。
	 * 
	 * @return
	 */
	public Writer getOut();
}
