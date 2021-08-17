/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/**
 * JS图表渲染器。
 * <p>
 * 它封装JS图表渲染器对象代码。
 * </p>
 * <p>
 * {@linkplain HtmlChartPlugin}使用它在HTML端渲染图表。
 * </p>
 * <p>
 * 它的格式应为：
 * </p>
 * <code>
 * <pre>
 * {
 * 	...,
 * 	render : function(chart){ ... },
 * 	...
 * }
 * </pre>
 * </code>
 * 
 * @author datagear@163.com
 *
 */
public interface JsChartRenderer extends Serializable
{
	/** JS图表渲染器的渲染函数名 */
	public static final String RENDER_FUNCTION_NAME = "render";

	/**
	 * 获取JS图表渲染器对象代码输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	Reader getReader() throws IOException;
}
