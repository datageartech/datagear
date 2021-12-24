/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * 文本解析支持类。
 * 
 * @author datagear@163.com
 *
 */
public class TextParserSupport
{
	public TextParserSupport()
	{
		super();
	}

	/**
	 * 写完行注释后停止。
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public void writeAfterLineComment(Reader in, Writer out) throws IOException
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
	 * @throws IOException
	 */
	public void writeAfterBlockComment(Reader in, Writer out) throws IOException
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
	 * @param quoteChar
	 * @throws IOException
	 */
	public void writeAfterQuote(Reader in, Writer out, int quoteChar, int escapeChar) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == escapeChar)
				writeIfValid(out, in.read());
			else if (c == quoteChar)
			{
				break;
			}
		}
	}

	/**
	 * 写完引号后停止（比如：{@code "}、{@code '}），并将写入内容复制至{@code sb}。
	 * 
	 * @param in
	 * @param out
	 * @param quoteChar
	 * @param escapeChar
	 * @param sb
	 * @throws IOException
	 */
	public void writeAfterQuote(Reader in, Writer out, int quoteChar, int escapeChar, StringBuilder sb)
			throws IOException
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
	 * 是否是JavaScript引号：<code>'</code>、<code>"</code>、<code>`</code>。
	 * 
	 * @param c
	 * @return
	 */
	public boolean isJsQuote(int c)
	{
		return (c == '\'' || c == '"' || c == '`');
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
		if (sb == null || sb.length() == 0)
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

	/**
	 * 获取一个可用的初始{@linkplain StringBuilder}。
	 * <p>
	 * 如果{@code prev}不为{@code null}且为空，将直接返回它，否则，新建一个并返回。
	 * </p>
	 * 
	 * @param prev
	 * @return
	 */
	public StringBuilder availableStringBuilder(StringBuilder prev)
	{
		if (prev != null && prev.length() == 0)
			return prev;

		return new StringBuilder();
	}
}
