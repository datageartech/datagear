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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.util.TextParserSupport;

/**
 * HTML过滤器，过滤和处理HTML标签。
 * <p>
 * 解析规则：
 * </p>
 * <ul>
 * <li>标签名解析规则参考{@linkplain #readTagName(Reader, StringBuilder)}</li>
 * <li>标签结束符："&gt;"、"/&gt;"</li>
 * <li>HTML注释（"&lt;!----&gt;"）不会被解析为标签</li>
 * <li>"&lt;script&gt;...&lt;/script&gt;"之间的内容不会解析为标签，而是按照JS脚本文本处理</li>
 * <li>"&lt;style&gt;...&lt;/style&gt;"之间的内容不会解析为标签，而是按照CSS文本处理</li>
 * </ul>
 * <p>
 * 使用示例：
 * </p>
 * <code>
 * <pre>
 * HtmlFilter hf = new HtmlFilter();
 * 
 * Reader in = ...;
 * Writer out = ...;
 * hf.filter(in, new DefaultFilterHandler(out));
 * </pre>
 * </code>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlFilter extends TextParserSupport
{
	/**
	 * 标签起始符：{@code <}
	 */
	public static final char TAG_START_CHAR = '<';

	/**
	 * 标签起始符：{@code <}
	 */
	public static final String TAG_START_STR = "<";

	/**
	 * 标签结束符：{@code >}
	 */
	public static final char TAG_END_CHAR = '>';

	/**
	 * 标签结束符：{@code >}
	 */
	public static final String TAG_END_STR = ">";

	/**
	 * 自关闭标签结束符：{@code />}
	 */
	public static final String TAG_END_STR_SELF_CLOSE = "/>";

	public HtmlFilter()
	{
		super();
	}

	/**
	 * 执行过滤。
	 * <p>
	 * 注意：此方法执行完后，不会关闭输入/输出流。
	 * </p>
	 * 
	 * @param in
	 * @param handler
	 * @throws IOException
	 */
	public void filter(Reader in, FilterHandler handler) throws IOException
	{
		handler.beforeWrite(in);

		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (c == TAG_START_CHAR)
			{
				StringBuilder tagNameSb = createStringBuilder();
				String afterTagName = readTagName(in, tagNameSb);
				String tagName = tagNameSb.toString();

				// <!--
				if (tagName.startsWith("!--"))
				{
					filterAfterHtmlComment(in, handler, tagName, afterTagName);
				}
				else
				{
					String tagEnd = filterAfterTag(in, handler, tagName, afterTagName);

					if (!TAG_END_STR_SELF_CLOSE.equals(tagEnd))
					{
						// <script></script>、<style></style>之间按照普通文本解析
						if ("script".equalsIgnoreCase(tagName))
							filterAfterScriptCloseTag(in, handler);
						else if ("style".equalsIgnoreCase(tagName))
							filterAfterStyleCloseTag(in, handler);
					}
				}

				if (handler.isAborted())
					break;
			}
			else
				out.write(c);
		}

		handler.afterWrite(in);
	}

	/**
	 * 从流中读取标签名。
	 * <p>
	 * 读取规则如下所示：
	 * </p>
	 * <table>
	 * <tr>
	 * <td>输入&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
	 * <td>标签名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
	 * <td>返回值</td>
	 * </tr>
	 * <tr>
	 * <td>"name "</td>
	 * <td>"name"</td>
	 * <td>" "</td>
	 * </tr>
	 * <tr>
	 * <td>"name>"</td>
	 * <td>"name"</td>
	 * <td>">"</td>
	 * </tr>
	 * <tr>
	 * <td>"name"</td>
	 * <td>"name"</td>
	 * <td>null</td>
	 * </tr>
	 * <tr>
	 * <td>"name/>"</td>
	 * <td>"name"</td>
	 * <td>"/>"</td>
	 * </tr>
	 * <tr>
	 * <td>"/name "</td>
	 * <td>"/name"</td>
	 * <td>" "</td>
	 * </tr>
	 * <tr>
	 * <td>">"</td>
	 * <td>""</td>
	 * <td>">"</td>
	 * </tr>
	 * <tr>
	 * <td>" "</td>
	 * <td>""</td>
	 * <td>" "</td>
	 * </tr>
	 * <tr>
	 * <td>"/&gt;"</td>
	 * <td>"/"</td>
	 * <td>"&gt;"</td>
	 * </tr>
	 * <tr>
	 * <td>" /&gt;"</td>
	 * <td>""</td>
	 * <td>"/&gt;"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param in
	 * @param tagName
	 * @return 已读取的但不是标签名的字符串：空白、{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String readTagName(Reader in, StringBuilder tagName) throws IOException
	{
		String next = null;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			if (isWhitespace(c) || c == TAG_END_CHAR)
			{
				next = codePointToString(c);
				break;
			}
			else if (c == '/')
			{
				int c0 = in.read();

				if (c0 == TAG_END_CHAR)
				{
					if (isEmpty(tagName))
					{
						appendChar(tagName, c);
						next = TAG_END_STR;
					}
					else
						next = TAG_END_STR_SELF_CLOSE;

					break;
				}
				else if (isWhitespace(c0))
				{
					appendChar(tagName, c);
					next = codePointToString(c0);

					break;
				}
				else
				{
					appendChar(tagName, c);
					appendCharIfValid(tagName, c0);
				}
			}
			else
				appendChar(tagName, c);
		}

		return next;
	}

	/**
	 * 写完HTML注释结束符（{@code -->}）后停止。
	 * 
	 * @param in
	 * @param handler
	 * @param tagName
	 * @param afterTagName
	 * @throws IOException
	 */
	protected void filterAfterHtmlComment(Reader in, FilterHandler handler, String tagName,
			String afterTagName) throws IOException
	{
		Writer out = handler.getOut();

		out.write(TAG_START_CHAR);
		out.write(tagName);
		writeIfNonNull(out, afterTagName);

		// <!---->
		// <!--comment-->
		if (TAG_END_STR.equals(afterTagName) && tagName.length() >= "!----".length() && tagName.endsWith("--"))
			;
		else
			filterAfterHtmlComment(in, handler);
	}

	/**
	 * 写完HTML注释结束符（{@code -->}）后停止。
	 * 
	 * @param in
	 * @param handler
	 * @throws IOException
	 */
	protected void filterAfterHtmlComment(Reader in, FilterHandler handler) throws IOException
	{
		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '-')
			{
				c = in.read();
				writeIfValid(out, c);

				if (c == '-')
				{
					c = in.read();
					writeIfValid(out, c);

					if (c == TAG_END_CHAR)
						break;
				}
			}
		}
	}

	/**
	 * 写完标签结束符（{@code ">"}、{@code "/>"}）后停止。
	 * 
	 * @param in
	 * @param handler
	 * @param tagName
	 * @param afterTagName 参考{@linkplain #readTagName(Reader, StringBuilder)}的返回值
	 * @return 标签结束符，可能为：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String filterAfterTag(Reader in, FilterHandler handler, String tagName, String afterTagName)
			throws IOException
	{
		Writer out = handler.getOut();

		handler.beforeWriteTagStart(in, tagName);

		out.write(TAG_START_CHAR);

		Map<String, String> tagAttrs = Collections.emptyMap();

		if (afterTagName == null)
		{
			out.write(tagName);
			handler.beforeWriteTagEnd(in, tagName, null, tagAttrs);

			return afterTagName;
		}
		else if (afterTagName.equals(TAG_END_STR) || afterTagName.equals(TAG_END_STR_SELF_CLOSE))
		{
			out.write(tagName);
			handler.beforeWriteTagEnd(in, tagName, afterTagName, tagAttrs);
			out.write(afterTagName);
			handler.afterWriteTagEnd(in, tagName, afterTagName);

			return afterTagName;
		}
		else
		{
			out.write(tagName);
			out.write(afterTagName);

			String tagEnd = null;

			if (handler.isResolveTagAttrs(in, tagName))
			{
				tagAttrs = handler.availableTagAttrs();
				tagEnd = filterUntilTagEnd(in, handler, tagName, tagAttrs);
				tagAttrs = Collections.unmodifiableMap(tagAttrs);
			}
			else
			{
				tagEnd = filterUntilTagEnd(in, handler, tagName);
			}

			handler.beforeWriteTagEnd(in, tagName, tagEnd, tagAttrs);

			if (tagEnd != null)
			{
				out.write(tagEnd);
				handler.afterWriteTagEnd(in, tagName, tagEnd);
			}

			return tagEnd;
		}
	}

	/**
	 * 写到标签结束符时停止。
	 * 
	 * @param in
	 * @param handler
	 * @param tagName
	 * @return 未写入输出流的标签结束符：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String filterUntilTagEnd(Reader in, FilterHandler handler, String tagName) throws IOException
	{
		String tagEnd = null;

		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (c == TAG_END_CHAR)
			{
				tagEnd = codePointToString(c);
				break;
			}
			else if (c == '\'' || c == '"')
			{
				out.write(c);
				writeAfterQuote(in, out, c, -1);
			}
			else if (c == '/')
			{
				int c0 = in.read();

				if (c0 == TAG_END_CHAR)
				{
					tagEnd = TAG_END_STR_SELF_CLOSE;
					break;
				}
				else if (isWhitespace(c0))
				{
					out.write(c);
					out.write(c0);
				}
				else
				{
					out.write(c);
					writeIfValid(out, c0);
				}
			}
			else
			{
				out.write(c);
			}
		}

		return tagEnd;
	}

	/**
	 * 写到标签结束符时停止。
	 * 
	 * @param in
	 * @param handler
	 * @param tagName
	 * @param tagAttrs
	 * @return 未写入输出流的标签结束符：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String filterUntilTagEnd(Reader in, FilterHandler handler, String tagName,
			Map<String, String> tagAttrs) throws IOException
	{
		String tagEnd = null;

		List<String> tagAttrTokens = handler.availableTagAttrTokens();
		StringBuilder token = createStringBuilder();

		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (c == TAG_END_CHAR)
			{
				tagEnd = codePointToString(c);
				break;
			}
			if (isWhitespace(c))
			{
				out.write(c);

				if (isNotEmpty(token))
				{
					tagAttrTokens.add(token.toString());
					token = createStringBuilder();
				}
			}
			else if (c == '=')
			{
				out.write(c);

				if (isNotEmpty(token))
				{
					tagAttrTokens.add(token.toString());
					tagAttrTokens.add("=");
					token = createStringBuilder();
				}
				else
					tagAttrTokens.add("=");
			}
			else if (c == '/')
			{
				int c0 = in.read();

				if (c0 == TAG_END_CHAR)
				{
					tagEnd = TAG_END_STR_SELF_CLOSE;
					break;
				}
				else if (isWhitespace(c0))
				{
					out.write(c);
					out.write(c0);

					appendChar(token, c);
					tagAttrTokens.add(token.toString());
					token = createStringBuilder();
				}
				else
				{
					out.write(c);
					writeIfValid(out, c0);

					appendChar(token, c);
					appendCharIfValid(token, c0);
				}
			}
			else if (c == '\'' || c == '"')
			{
				out.write(c);
				appendChar(token, c);

				writeAfterQuote(in, out, c, -1, token);
			}
			else
			{
				out.write(c);
				appendChar(token, c);
			}
		}

		if (isNotEmpty(token))
			tagAttrTokens.add(token.toString());

		resolveTagAttrs(tagAttrTokens, tagAttrs);

		return tagEnd;
	}

	/**
	 * 解析标签属性映射表。
	 * 
	 * @param attrTokens
	 * @param tagAttrs
	 */
	protected void resolveTagAttrs(List<String> attrTokens, Map<String, String> tagAttrs)
	{
		for (int i = 0, len = attrTokens.size(); i < len; i++)
		{
			String str = attrTokens.get(i);
			String str1 = ((i + 1) < len ? attrTokens.get(i + 1) : null);
			String str2 = ((i + 2) < len ? attrTokens.get(i + 2) : null);

			if ("=".equals(str1))
			{
				tagAttrs.put(str, deleteQuote(str2));
				i = i + 2;
			}
			else
				tagAttrs.put(str, null);
		}
	}

	/**
	 * 写完"&lt;/script&gt;"后停止。
	 * 
	 * @param in
	 * @param handler
	 */
	protected void filterAfterScriptCloseTag(Reader in, FilterHandler handler) throws IOException
	{
		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			// 字符串
			if (isJsQuote(c))
			{
				out.write(c);
				writeAfterQuote(in, out, c, '\\');
			}
			else if (c == '/')
			{
				out.write(c);

				int c0 = in.read();
				writeIfValid(out, c0);

				// 行注释
				if (c0 == '/')
				{
					writeAfterLineComment(in, out);
				}
				// 块注释
				else if (c0 == '*')
				{
					writeAfterBlockComment(in, out);
				}
			}
			else if (c == TAG_START_CHAR)
			{
				StringBuilder tagNameSb = createStringBuilder();
				String afterTagName = readTagName(in, tagNameSb);
				String tagName = tagNameSb.toString();

				if ("/script".equalsIgnoreCase(tagName))
				{
					filterAfterTag(in, handler, tagName, afterTagName);
					break;
				}
				else
				{
					out.write(c);
					out.write(tagName);
					writeIfNonNull(out, afterTagName);
				}
			}
			else
				out.write(c);
		}
	}

	/**
	 * 写完"&lt;/style&gt;"后停止。
	 * 
	 * @param in
	 * @param handler
	 */
	protected void filterAfterStyleCloseTag(Reader in, FilterHandler handler) throws IOException
	{
		Writer out = handler.getOut();
		int c = -1;

		while ((c = in.read()) > -1)
		{
			// 字符串
			if (c == '\'' || c == '"')
			{
				out.write(c);
				writeAfterQuote(in, out, c, '\\');
			}
			else if (c == '/')
			{
				out.write(c);

				int c0 = in.read();
				writeIfValid(out, c0);

				// 行注释
				if (c0 == '/')
				{
					writeAfterLineComment(in, out);
				}
				// 块注释
				else if (c0 == '*')
				{
					writeAfterBlockComment(in, out);
				}
			}
			else if (c == TAG_START_CHAR)
			{
				StringBuilder tagNameSb = createStringBuilder();
				String afterTagName = readTagName(in, tagNameSb);
				String tagName = tagNameSb.toString();

				if ("/style".equalsIgnoreCase(tagName))
				{
					filterAfterTag(in, handler, tagName, afterTagName);
					break;
				}
				else
				{
					out.write(c);
					out.write(tagName);
					writeIfNonNull(out, afterTagName);
				}
			}
			else
				out.write(c);
		}
	}
}
