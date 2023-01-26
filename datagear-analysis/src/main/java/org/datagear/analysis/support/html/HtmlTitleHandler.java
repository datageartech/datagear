/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

/**
 * HTML的<code>&lt;title&gt;&lt;/title&gt;</code>处理器。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlTitleHandler
{
	/**
	 * 返回要追加的标题内容。
	 * 
	 * @param title
	 * @return
	 */
	String suffix(String title);
}
