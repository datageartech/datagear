/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;

/**
 * 图表脚本内容。
 * <p>
 * {@linkplain HtmlChartPlugin}使用它获取图表渲染脚本内容。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ScriptContent
{
	/**
	 * 获取图表脚本内容输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	Reader getReader() throws IOException;
}
