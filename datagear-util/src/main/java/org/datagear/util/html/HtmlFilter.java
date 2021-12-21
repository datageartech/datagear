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
public class HtmlFilter
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
	 *            HTML输入流
	 * @throws IOException
	 */
	public void filter(Reader in) throws IOException
	{
		filter(in, new DefaultFilterHandler(NopWriter.NOP_WRITER));
	}

	/**
	 * 执行过滤。
	 * <p>
	 * 注意：此方法执行完后，不会关闭输入/输出流。
	 * </p>
	 * 
	 * @param in
	 *            HTML输入流
	 * @param out
	 *            HTML输出流
	 * @throws IOException
	 */
	public void filter(Reader in, Writer out) throws IOException
	{
		filter(in, new DefaultFilterHandler(out));
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
				handler.write(c);
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
		handler.write(TAG_START_CHAR);
		handler.write(tagName);
		writeIfNonNull(handler, afterTagName);

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
		int c = -1;
		while ((c = in.read()) > -1)
		{
			handler.write(c);

			if (c == '-')
			{
				c = in.read();
				writeIfValid(handler, c);

				if (c == '-')
				{
					c = in.read();
					writeIfValid(handler, c);

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
		handler.beforeWriteTagStart(in, tagName);

		handler.write(TAG_START_CHAR);

		Map<String, String> tagAttrs = Collections.emptyMap();

		if (afterTagName == null)
		{
			handler.write(tagName);
			handler.beforeWriteTagEnd(in, tagName, null, tagAttrs);

			return afterTagName;
		}
		else if (afterTagName.equals(TAG_END_STR) || afterTagName.equals(TAG_END_STR_SELF_CLOSE))
		{
			handler.write(tagName);
			handler.beforeWriteTagEnd(in, tagName, afterTagName, tagAttrs);
			handler.write(afterTagName);
			handler.afterWriteTagEnd(in, tagName, afterTagName);

			return afterTagName;
		}
		else
		{
			handler.write(tagName);
			handler.write(afterTagName);

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
				handler.write(tagEnd);
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
				handler.write(c);
				filterAfterQuote(in, handler, c, -1);
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
					handler.write(c);
					handler.write(c0);
				}
				else
				{
					handler.write(c);
					writeIfValid(handler, c0);
				}
			}
			else
			{
				handler.write(c);
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
				handler.write(c);

				if (isNotEmpty(token))
				{
					tagAttrTokens.add(token.toString());
					token = createStringBuilder();
				}
			}
			else if (c == '=')
			{
				handler.write(c);

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
					handler.write(c);
					handler.write(c0);

					appendChar(token, c);
					tagAttrTokens.add(token.toString());
					token = createStringBuilder();
				}
				else
				{
					handler.write(c);
					writeIfValid(handler, c0);

					appendChar(token, c);
					appendCharIfValid(token, c0);
				}
			}
			else if (c == '\'' || c == '"')
			{
				handler.write(c);
				appendChar(token, c);

				filterAfterQuote(in, handler, c, -1, token);
			}
			else
			{
				handler.write(c);
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
		int c = -1;
		while ((c = in.read()) > -1)
		{
			// 字符串
			if (c == '\'' || c == '"' || c == '`')
			{
				handler.write(c);
				filterAfterQuote(in, handler, c, '\\');
			}
			else if (c == '/')
			{
				handler.write(c);

				int c0 = in.read();
				writeIfValid(handler, c0);

				// 行注释
				if (c0 == '/')
				{
					filterAfterLineComment(in, handler);
				}
				// 块注释
				else if (c0 == '*')
				{
					filterAfterBlockComment(in, handler);
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
					handler.write(c);
					handler.write(tagName);
					writeIfNonNull(handler, afterTagName);
				}
			}
			else
				handler.write(c);
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
		int c = -1;
		while ((c = in.read()) > -1)
		{
			// 字符串
			if (c == '\'' || c == '"')
			{
				handler.write(c);
				filterAfterQuote(in, handler, c, '\\');
			}
			else if (c == '/')
			{
				handler.write(c);

				int c0 = in.read();
				writeIfValid(handler, c0);

				// 行注释
				if (c0 == '/')
				{
					filterAfterLineComment(in, handler);
				}
				// 块注释
				else if (c0 == '*')
				{
					filterAfterBlockComment(in, handler);
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
					handler.write(c);
					handler.write(tagName);
					writeIfNonNull(handler, afterTagName);
				}
			}
			else
				handler.write(c);
		}
	}

	/**
	 * 写完行注释后停止。
	 * 
	 * @param in
	 * @param handler
	 * @throws IOException
	 */
	protected void filterAfterLineComment(Reader in, FilterHandler handler) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			handler.write(c);

			if (c == '\n' || c == '\r')
				break;
		}
	}

	/**
	 * 写完块注释后停止。
	 * 
	 * @param in
	 * @param handler
	 * @throws IOException
	 */
	protected void filterAfterBlockComment(Reader in, FilterHandler handler) throws IOException
	{
		int c = -1;
		while ((c = in.read()) > -1)
		{
			handler.write(c);

			if (c == '*')
			{
				int c0 = in.read();
				writeIfValid(handler, c0);

				if (c0 == '/')
					break;
			}
		}
	}

	/**
	 * 写完引号后停止（比如：{@code "}、{@code '}）。
	 * 
	 * @param in
	 * @param handler
	 * @param quoteChar
	 * @throws IOException
	 */
	protected void filterAfterQuote(Reader in, FilterHandler handler, int quoteChar, int escapeChar)
			throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			handler.write(c);

			if(c == escapeChar)
				writeIfValid(handler, in.read());
			else if (c == quoteChar)
			{
				break;
			}
		}
	}

	/**
	 * 写完引号后停止（比如：{@code "}、{@code '}）。
	 * 
	 * @param in
	 * @param handler
	 * @param quoteChar
	 * @throws IOException
	 */
	protected void filterAfterQuote(Reader in, FilterHandler handler, int quoteChar, int escapeChar,
			StringBuilder sb) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			handler.write(c);
			appendChar(sb, c);

			if (c == escapeChar)
			{
				int c0 = in.read();
				writeIfValid(handler, c0);
				appendCharIfValid(sb, c0);
			}
			else if (c == quoteChar)
			{
				break;
			}
		}
	}

	/**
	 * 追加字符。
	 * 
	 * @param sb
	 * @param c
	 */
	public void appendChar(StringBuilder sb, int c)
	{
		sb.appendCodePoint(c);
	}

	/**
	 * 追加字符。
	 * 
	 * @param sb
	 * @param c
	 */
	public void appendCharIfValid(StringBuilder sb, int c)
	{
		if (c > -1)
			sb.appendCodePoint(c);
	}

	/**
	 * 仅在 {@code c > -1} 时写入。
	 * 
	 * @param c
	 * @throws IOException
	 */
	public void writeIfValid(FilterHandler handler, int c) throws IOException
	{
		if (c > -1)
			handler.write(c);
	}

	/**
	 * 仅在 {@code str != null} 时写入。
	 * <p>
	 * 它内部调用{@linkplain #write(String)}。
	 * </p>
	 * 
	 * @param str
	 * @throws IOException
	 */
	public void writeIfNonNull(FilterHandler handler, String str) throws IOException
	{
		if (str != null)
			handler.write(str);
	}

	/**
	 * 删除字符串的首尾引号。
	 * 
	 * @param str
	 * @return
	 */
	public String deleteQuote(String str)
	{
		if (str == null)
			return str;

		int len = str.length();

		if (len < 2)
			return str;

		if (str.charAt(0) == '\'')
		{
			if (str.charAt(len - 1) == '\'')
				return str.substring(1, len - 1);
			else
				return str;
		}
		else if (str.charAt(0) == '"')
		{
			if (str.charAt(len - 1) == '"')
				return str.substring(1, len - 1);
			else
				return str;
		}
		else
			return str;
	}

	/**
	 * 是否空格字符。
	 * 
	 * @param c
	 * @return
	 */
	public boolean isWhitespace(int c)
	{
		return Character.isWhitespace(c);
	}

	/**
	 * 码点转换为字符串。
	 * 
	 * @param codePoint
	 * @return
	 */
	public String codePointToString(int codePoint)
	{
		return new String(Character.toChars(codePoint));
	}

	/**
	 * {@linkplain StringBuilder}是否为空。
	 * 
	 * @param sb
	 * @return
	 */
	public boolean isEmpty(StringBuilder sb)
	{
		return (sb == null || sb.length() == 0);
	}

	/**
	 * {@linkplain StringBuilder}是否不为空。
	 * 
	 * @param sb
	 * @return
	 */
	public boolean isNotEmpty(StringBuilder sb)
	{
		return (sb != null && sb.length() > 0);
	}

	/**
	 * 清除{@linkplain StringBuilder}。
	 * 
	 * @param sb
	 */
	public void clear(StringBuilder sb)
	{
		if (sb == null || isEmpty(sb))
			return;

		sb.delete(0, sb.length());
	}

	/**
	 * 创建{@linkplain StringBuilder}。
	 * 
	 * @return
	 */
	public StringBuilder createStringBuilder()
	{
		return new StringBuilder();
	}
}
