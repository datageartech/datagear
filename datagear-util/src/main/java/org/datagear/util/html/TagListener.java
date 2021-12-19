/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * HTML标签监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface TagListener
{
	/**
	 * 标签起始符（{@code '<'}）写入输出流前置处理函数。
	 * <p>
	 * 子类可以扩展此方法，在标签前插入内容。
	 * </p>
	 * 
	 * @param in
	 * @param out
	 * @param tagName 标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}，
	 *                详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @throws IOException
	 */
	void beforeTagStart(Reader in, Writer out, String tagName) throws IOException;

	/**
	 * 是否解析指定标签的属性集。
	 * <p>
	 * 解析的属性集将被传递给{@linkplain #beforeWriteTagEnd(Reader, Writer, String, String, Map)}的{@code attrs}参数。
	 * </p>
	 * 
	 * @param in
	 * @param out
	 * @param tagName 标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *                详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @return
	 */
	boolean isResolveTagAttrs(Reader in, Writer out, String tagName);

	/**
	 * 标签结束符写入输出流前置处理函数。
	 * <p>
	 * 子类可以扩展此方法，在标签内插入属性。
	 * </p>
	 * 
	 * @param in
	 * @param out
	 * @param tagName 标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *                详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @param tagEnd  标签结束符，可能为：{@code ">"}、{@code "/>"}、{@code null}
	 * @param attrs   当{@linkplain #isResolveTagAttrs(String)}为{@code false}时，将是空映射表；当为{@code true}时，则是解析的属性映射表。
	 * @throws IOException
	 */
	void beforeTagEnd(Reader in, Writer out, String tagName, String tagEnd, Map<String, String> attrs)
			throws IOException;

	/**
	 * 标签结束符写入输出流后置处理函数。
	 * <p>
	 * 子类可以扩展此方法，在标签后插入内容。
	 * </p>
	 * 
	 * @param in
	 * @param out
	 * @param tagName 标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *                详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @param tagEnd  标签结束符，可能为：{@code ">"}、{@code "/>"}
	 * @return 是否中止过滤，{@code true} 是；{@code false} 否
	 * @throws IOException
	 */
	boolean afterTagEnd(Reader in, Writer out, String tagName, String tagEnd) throws IOException;
}
