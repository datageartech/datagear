/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlFilter
{
	public HtmlFilter()
	{
		super();
	}

	/**
	 * 执行过滤。
	 * 
	 * @param htmlReader
	 * @param htmlWriter
	 * @throws IOException
	 */
	public void filter(Reader htmlReader, Writer htmlWriter) throws IOException
	{
		filter(htmlReader, htmlWriter, new FilterContext());
	}

	/**
	 * 执行过滤。
	 * 
	 * @param htmlReader
	 * @throws IOException
	 */
	public void filter(Reader htmlReader) throws IOException
	{
		filter(htmlReader, NopWriter.NOP_WRITER, new FilterContext());
	}

	/**
	 * 执行过滤。
	 * 
	 * @param htmlReader
	 * @param htmlWriter
	 * @param tagListener 允许为{@code null}
	 * @throws IOException
	 */
	public void filter(Reader htmlReader, Writer htmlWriter, TagListener tagListener) throws IOException
	{
		filter(htmlReader, htmlWriter, new FilterContext(tagListener));
	}

	/**
	 * 执行过滤。
	 * 
	 * @param htmlReader
	 * @param tagListener 允许为{@code null}
	 * @throws IOException
	 */
	public void filter(Reader htmlReader, TagListener tagListener) throws IOException
	{
		filter(htmlReader, NopWriter.NOP_WRITER, new FilterContext(tagListener));
	}

	/**
	 * 执行过滤。
	 * 
	 * @param htmlReader
	 * @param htmlWriter
	 * @param context
	 * @throws IOException
	 */
	protected void filter(Reader htmlReader, Writer htmlWriter, FilterContext context) throws IOException
	{
		BufferedReader in = (htmlReader instanceof BufferedReader ? (BufferedReader) htmlReader
				: new BufferedReader(htmlReader));
		BufferedWriter out = (htmlWriter instanceof BufferedWriter ? (BufferedWriter) htmlWriter
				: new BufferedWriter(htmlWriter));

		int c = -1;
		while ((c = in.read()) > -1)
		{
			if (c == '<')
			{
				StringBuilder tagNameSb = createStringBuilder();
				String afterTagName = readTagName(in, tagNameSb);
				String tagName = tagNameSb.toString();

				// <!--
				if (tagName.startsWith("!--"))
				{
					writeAfterHtmlComment(in, out, context, tagName, afterTagName);
				}
				else
				{
					String tagEnd = writeAfterTag(in, out, context, tagName, afterTagName);

					// "<script..."且非"<script.../>"
					if (!"/>".equals(tagEnd) && tagName.equalsIgnoreCase("script"))
					{
						writeAfterScriptCloseTag(in, out, context);
					}
				}

				if (context.isAborted())
					break;
			}
			else
				out.write(c);
		}

		out.flush();
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
			if (isWhitespace(c) || c == '>')
			{
				next = codePointToString(c);
				break;
			}
			else if (c == '/')
			{
				int c0 = in.read();

				if (c0 == '>')
				{
					if (isEmpty(tagName))
					{
						appendChar(tagName, c);
						next = ">";
					}
					else
						next = "/>";

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
	 * @param out
	 * @param context
	 * @param tagName
	 * @param afterTagName
	 * @throws IOException
	 */
	protected void writeAfterHtmlComment(Reader in, Writer out, FilterContext context, String tagName,
			String afterTagName) throws IOException
	{
		out.write('<');
		out.write(tagName);
		writeIfNonNull(out, afterTagName);

		// <!---->
		// <!--comment-->
		if (">".equals(afterTagName) && tagName.length() >= "!----".length() && tagName.endsWith("--"))
			;
		else
			writeAfterHtmlComment(in, out, context);
	}

	/**
	 * 写完HTML注释结束符（{@code -->}）后停止。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	protected void writeAfterHtmlComment(Reader in, Writer out, FilterContext context) throws IOException
	{
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

					if (c == '>')
						break;
				}
			}
		}
	}

	/**
	 * 写完标签结束符（{@code ">"}、{@code "/>"}）后停止。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @param afterTagName 参考{@linkplain #readTagName(Reader, StringBuilder)}的返回值
	 * @return 标签结束符，可能为：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String writeAfterTag(Reader in, Writer out, FilterContext context, String tagName, String afterTagName)
			throws IOException
	{
		beforeWriteTagStart(in, out, context, tagName);

		out.write('<');

		Map<String, String> tagAttrs = Collections.emptyMap();

		if (afterTagName == null)
		{
			out.write(tagName);
			beforeWriteTagEnd(in, out, context, tagName, null, tagAttrs);

			return afterTagName;
		}
		else if (afterTagName.equals(">") || afterTagName.equals("/>"))
		{
			out.write(tagName);
			beforeWriteTagEnd(in, out, context, tagName, afterTagName, tagAttrs);
			out.write(afterTagName);
			afterWriteTagEnd(in, out, context, tagName, afterTagName);

			return afterTagName;
		}
		else
		{
			out.write(tagName);
			out.write(afterTagName);

			String tagEnd = null;

			if (isResolveTagAttrs(in, out, context, tagName))
			{
				tagAttrs = context.availableTagAttrs();
				tagEnd = writeUntilTagEnd(in, out, context, tagName, tagAttrs);
				tagAttrs = Collections.unmodifiableMap(tagAttrs);
			}
			else
			{
				tagEnd = writeUntilTagEnd(in, out, context, tagName);
			}

			beforeWriteTagEnd(in, out, context, tagName, tagEnd, tagAttrs);

			if (tagEnd != null)
			{
				out.write(tagEnd);
				afterWriteTagEnd(in, out, context, tagName, tagEnd);
			}

			return tagEnd;
		}
	}

	/**
	 * 写到标签结束符时停止。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @return 未写入输出流的标签结束符：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String writeUntilTagEnd(Reader in, Writer out, FilterContext context, String tagName) throws IOException
	{
		String tagEnd = null;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			if (c == '>')
			{
				tagEnd = codePointToString(c);
				break;
			}
			else if (c == '\'' || c == '"')
			{
				out.write(c);
				writeAfterQuote(in, out, context, c, -1);
			}
			else if (c == '/')
			{
				int c0 = in.read();

				if (c0 == '>')
				{
					tagEnd = "/>";
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
	 * @param out
	 * @param context
	 * @param tagName
	 * @param tagAttrs
	 * @return 未写入输出流的标签结束符：{@code ">"}、{@code "/>"}、{@code null}
	 * @throws IOException
	 */
	protected String writeUntilTagEnd(Reader in, Writer out, FilterContext context, String tagName,
			Map<String, String> tagAttrs) throws IOException
	{
		String tagEnd = null;

		List<String> tagAttrTokens = context.availableTagAttrTokens();
		StringBuilder token = createStringBuilder();

		int c = -1;

		while ((c = in.read()) > -1)
		{
			if (c == '>')
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

				if (c0 == '>')
				{
					tagEnd = "/>";
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

				writeAfterQuote(in, out, context, c, -1, token);
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
	 * @param out
	 * @param context
	 */
	protected void writeAfterScriptCloseTag(Reader in, Writer out, FilterContext context) throws IOException
	{
		int c = -1;
		while ((c = in.read()) > -1)
		{
			// 字符串
			if (c == '\'' || c == '"' || c == '`')
			{
				out.write(c);
				writeAfterQuote(in, out, context, c, '\\');
			}
			else if (c == '/')
			{
				out.write(c);

				int c0 = in.read();
				writeIfValid(out, c0);

				// 行注释
				if (c0 == '/')
				{
					writeAfterLineComment(in, out, context);
				}
				// 块注释
				else if (c0 == '*')
				{
					writeAfterBlockComment(in, out, context);
				}
			}
			else if (c == '<')
			{
				StringBuilder tagNameSb = createStringBuilder();
				String afterTagName = readTagName(in, tagNameSb);
				String tagName = tagNameSb.toString();

				if ("/script".equalsIgnoreCase(tagName))
				{
					writeAfterTag(in, out, context, tagName, afterTagName);
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
	 * 写完行注释后停止。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	protected void writeAfterLineComment(Reader in, Writer out, FilterContext context) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '\n' || c == '\r')
				break;
		}
	}

	/**
	 * 写完块注释后停止。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	protected void writeAfterBlockComment(Reader in, Writer out, FilterContext context) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '*')
			{
				int c0 = in.read();
				writeIfValid(out, c0);

				if (c0 == '/')
					break;
			}
		}
	}

	/**
	 * 写完引号后停止（比如：{@code "}、{@code '}）。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param quoteChar
	 * @throws IOException
	 */
	protected void writeAfterQuote(Reader in, Writer out, FilterContext context, int quoteChar, int escapeChar)
			throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if(c == escapeChar)
				writeIfValid(out, in.read());
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
	 * @param out
	 * @param context
	 * @param quoteChar
	 * @throws IOException
	 */
	protected void writeAfterQuote(Reader in, Writer out, FilterContext context, int quoteChar, int escapeChar,
			StringBuilder sb) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);
			appendChar(sb, c);

			if (c == escapeChar)
			{
				int c0 = in.read();
				writeIfValid(out, c0);
				appendCharIfValid(sb, c0);
			}
			else if (c == quoteChar)
			{
				break;
			}
		}
	}

	/**
	 * 标签起始符（{@code '<'}）写入输出流前置处理函数。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @throws IOException
	 * @see {@linkplain TagListener#beforeTagStart(Reader, Writer, String)}
	 */
	protected void beforeWriteTagStart(Reader in, Writer out, FilterContext context, String tagName) throws IOException
	{
		TagListener tl = context.getTagListener();
		if (tl != null)
			tl.beforeTagStart(in, out, tagName);
	}

	/**
	 * 是否解析指定标签的属性集。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @return
	 * @see {@linkplain TagListener#isResolveTagAttrs(String)}
	 */
	protected boolean isResolveTagAttrs(Reader in, Writer out, FilterContext context, String tagName)
	{
		TagListener tl = context.getTagListener();
		return (tl != null ? tl.isResolveTagAttrs(in, out, tagName) : false);
	}

	/**
	 * 标签结束符写入输出流前置处理函数。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @param tagEnd
	 * @param attrs
	 * @throws IOException
	 * @see {@linkplain TagListener#beforeTagEnd(Reader, Writer, String, String, Map)}
	 */
	protected void beforeWriteTagEnd(Reader in, Writer out, FilterContext context, String tagName, String tagEnd,
			Map<String, String> attrs)
			throws IOException
	{
		TagListener tl = context.getTagListener();
		if (tl != null)
			tl.beforeTagEnd(in, out, tagName, tagEnd, attrs);
	}

	/**
	 * 标签结束符写入输出流后置处理函数。
	 * 
	 * @param in
	 * @param out
	 * @param context
	 * @param tagName
	 * @param tagEnd
	 * @throws IOException
	 * @see {@linkplain TagListener#afterTagEnd(Reader, Writer, String, String)}
	 */
	protected void afterWriteTagEnd(Reader in, Writer out, FilterContext context, String tagName, String tagEnd)
			throws IOException
	{
		TagListener tl = context.getTagListener();
		if (tl != null)
		{
			boolean aborted = tl.afterTagEnd(in, out, tagName, tagEnd);

			if (aborted && !context.isAborted())
				context.setAborted(aborted);
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
	 * @param out
	 * @param c
	 * @throws IOException
	 */
	public void writeIfValid(Writer out, int c) throws IOException
	{
		if (c > -1)
			out.write(c);
	}

	/**
	 * 仅在 {@code str != null} 时写入。
	 * 
	 * @param out
	 * @param str
	 * @throws IOException
	 */
	public void writeIfNonNull(Writer out, String str) throws IOException
	{
		if (str != null)
			out.write(str);
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
	 * 给定标签名是否是关闭标签名，即：以{@code '/'}字符开头。
	 * 
	 * @param tagName
	 * @return
	 */
	public boolean isCloseTagName(String tagName)
	{
		return (tagName != null && tagName.length() > 0 && tagName.charAt(0) == '/');
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

	protected static class FilterContext
	{
		private TagListener tagListener = null;

		private boolean aborted = false;

		private Map<String, String> prevTagAttrs = new HashMap<String, String>();

		private List<String> prevTagAttrTokens = new ArrayList<String>();

		public FilterContext()
		{
			super();
		}

		/**
		 * 
		 * @param tagListener 允许为{@code null}
		 */
		public FilterContext(TagListener tagListener)
		{
			super();
			this.tagListener = tagListener;
		}

		/**
		 * 
		 * @return 可能为{@code null}
		 */
		public TagListener getTagListener()
		{
			return tagListener;
		}

		public void setTagListener(TagListener tagListener)
		{
			this.tagListener = tagListener;
		}

		public boolean isAborted()
		{
			return aborted;
		}

		public void setAborted(boolean aborted)
		{
			this.aborted = aborted;
		}

		/**
		 * 获取一个可用与存储新标签属性的映射表。
		 * 
		 * @return
		 */
		public Map<String, String> availableTagAttrs()
		{
			if (!this.prevTagAttrs.isEmpty())
				this.prevTagAttrs = new HashMap<String, String>();

			return this.prevTagAttrs;
		}

		/**
		 * 获取一个可用与存储新标签属性Token的列表。
		 * 
		 * @return
		 */
		public List<String> availableTagAttrTokens()
		{
			if (!this.prevTagAttrTokens.isEmpty())
				this.prevTagAttrTokens = new ArrayList<String>();

			return this.prevTagAttrTokens;
		}
	}
}
