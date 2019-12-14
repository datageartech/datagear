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

	/**
	 * 生成下一个序号。
	 * <p>
	 * 此方法为生成HTML页面元素ID、变量名提供支持。
	 * </p>
	 * 
	 * @return
	 */
	int nextSequence();
}
