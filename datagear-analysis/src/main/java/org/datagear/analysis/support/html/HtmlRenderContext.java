/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.RenderContext;

/**
 * HTML渲染上下文。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlRenderContext extends RenderContext
{
	/**
	 * 获取渲染输出流。
	 * 
	 * @return
	 */
	Writer getWriter();
}
