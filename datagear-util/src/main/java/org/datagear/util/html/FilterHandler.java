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
 * HTML过滤处理器，用于处理{@linkplain HtmlFilter}过滤的HTML内容。
 * 
 * @author datagear@163.com
 *
 */
public interface FilterHandler
{
	/**
	 * 获取HTML输出流。
	 * <p>
	 * 这个输出流将用于输出过滤的HTML内容。
	 * </p>
	 * <p>
	 * 如果不需要输出，可以返回{@linkplain NopWriter}。
	 * </p>
	 * 
	 * @return
	 */
	Writer getOut();

	/**
	 * 获取一个可用于存储新标签属性的映射表。
	 * 
	 * @return
	 */
	Map<String, String> availableTagAttrs();

	/**
	 * 获取一个可用于存储新标签属性Token的列表。
	 * 
	 * @return
	 */
	List<String> availableTagAttrTokens();

	/**
	 * 是否中止过滤。
	 * <p>
	 * 如果为{@code true}，{@linkplain HtmlFilter}会在{@linkplain #afterTagEnd(Reader, Writer, String, String)}后中止。
	 * </p>
	 * 
	 * @return
	 */
	boolean isAborted();

	/**
	 * 开始写入前置处理函数。
	 * 
	 * @param in
	 * @throws IOException
	 */
	void beforeWrite(Reader in) throws IOException;

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
	 * @param attrs
	 *            当{@linkplain #isResolveTagAttrs(String)}为{@code true}时，会包含解析的标签属性映射表；否则，不包含。
	 * @throws IOException
	 */
	void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException;

	/**
	 * 完成写入后置处理函数，包括{@linkplain #isAborted()}的情形。
	 * 
	 * @param in
	 * @throws IOException
	 */
	void afterWrite(Reader in) throws IOException;

}
