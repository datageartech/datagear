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

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

/**
 * JS图表渲染器。
 * <p>
 * 它封装JS图表渲染器代码。
 * </p>
 * <p>
 * {@linkplain HtmlChartPlugin}使用它在HTML端渲染图表。
 * </p>
 * <p>
 * 代码格式参考：
 * <p>
 * {@linkplain #CODE_TYPE_OBJECT}、{@linkplain #CODE_TYPE_INVOKE}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface JsChartRenderer extends Serializable
{
	/** JS图表渲染器的渲染函数名 */
	public static final String RENDER_FUNCTION_NAME = "render";

	/**
	 * JS图表渲染器代码类型：对象。格式规范为： <code>
	 * <pre>
	 * {
	 * 	render: function(chart){ ... },
	 * 	...
	 * }
	 * </pre>
	 * </code>
	 */
	public static final String CODE_TYPE_OBJECT = "object";

	/**
	 * JS图表渲染器代码类型：调用。格式规范为： <code>
	 * <pre>
	 * (function(plugin)
	 * {
	 * 	...
	 * 	return [图表渲染器对象];
	 * })
	 * (plugin);
	 * </pre>
	 * </code>
	 * <p>
	 * 其中，{@code [图表渲染器对象]}格式规范同{@linkplain #CODE_TYPE_OBJECT}。
	 * </p>
	 * <p>
	 * 注意：上述代码中最后一行的{@code plugin}实参名不可变更， 第一行的{@code plugin}形参名则可随意定义。
	 * </p>
	 */
	public static final String CODE_TYPE_INVOKE = "invoke";

	/**
	 * {@linkplain #CODE_TYPE_INVOKE}格式规范中的上下文插件变量名。
	 */
	public static final String INVOKE_CONTEXT_PLUGIN_VAR = "plugin";

	/**
	 * 获取{@linkplain #getCodeReader()}中代码类型，参考：
	 * <p>
	 * {@linkplain #CODE_TYPE_OBJECT}、{@linkplain #CODE_TYPE_INVOKE}。
	 * </p>
	 * 
	 * @return
	 */
	String getCodeType();

	/**
	 * 获取JS图表渲染器代码输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	Reader getCodeReader() throws IOException;
}
