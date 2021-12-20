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
import java.util.List;
import java.util.Map;

/**
 * HTML过滤处理器。
 * 
 * @author datagear@163.com
 *
 */
public interface FilterHandler
{
	/**
	 * 写入从HTML输入流中读取的字符。
	 * 
	 * @param c
	 * @throws IOException
	 */
	public void write(char c) throws IOException;

	/**
	 * 写入从HTML输入流中读取的字符。
	 * 
	 * @param c
	 * @throws IOException
	 */
	public void write(int c) throws IOException;

	/**
	 * 写入从HTML输入流中读取的字符串。
	 * 
	 * @param str
	 * @throws IOException
	 */
	public void write(String str) throws IOException;

	/**
	 * 获取一个可用于存储新标签属性的映射表。
	 * 
	 * @return
	 */
	public Map<String, String> availableTagAttrs();

	/**
	 * 获取一个可用于存储新标签属性Token的列表。
	 * 
	 * @return
	 */
	public List<String> availableTagAttrTokens();

	/**
	 * 是否中止过滤。
	 * <p>
	 * 如果为{@code true}，{@linkplain HtmlFilter}会在{@linkplain #afterTagEnd(Reader, Writer, String, String)}后中止。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isAborted();

	/**
	 * 标签起始符（{@code '<'}）写入前置处理函数。
	 * 
	 * @param in
	 * @param tagName
	 *            标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}，
	 *            详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @throws IOException
	 */
	void beforeWriteTagStart(Reader in, String tagName) throws IOException;

	/**
	 * 是否解析指定标签的属性集。
	 * <p>
	 * 解析的属性集将被传递给{@linkplain #beforeWriteTagEnd(Reader, String, String, Map)}的{@code attrs}参数。
	 * </p>
	 * 
	 * @param in
	 * @param tagName
	 *            标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *            详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @return
	 */
	boolean isResolveTagAttrs(Reader in, String tagName);

	/**
	 * 标签结束符写入前置处理函数。
	 * 
	 * @param in
	 * @param tagName
	 *            标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *            详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @param tagEnd
	 *            标签结束符，可能为：{@code ">"}、{@code "/>"}、{@code null}
	 * @param attrs
	 *            当{@linkplain #isResolveTagAttrs(String)}为{@code true}时，会包含解析的标签属性映射表；否则，不包含。
	 * @throws IOException
	 */
	void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
			throws IOException;

	/**
	 * 标签结束符写入后置处理函数。
	 * 
	 * @param in
	 * @param tagName
	 *            标签名，可能为：{@code "..."}、{@code "/..."}、{@code ""}、{@code "/"}
	 *            详细参考{@linkplain HtmlFilter#readTagName(Reader, StringBuilder)}
	 * @param tagEnd
	 *            标签结束符，可能为：{@code ">"}、{@code "/>"}
	 * @throws IOException
	 */
	void afterWriteTagEnd(Reader in, String tagName, String tagEnd) throws IOException;
}
