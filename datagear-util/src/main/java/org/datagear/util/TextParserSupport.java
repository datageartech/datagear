/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	 * 将输入流写入输出流，直到写完行注释（<code>&#47&#47...</code>）后停止。
	 * 
	 * @param in
	 * @param out
	 * @return 行注释的下一个字符：{@code '\n'}、{@code '\r'}、{@code -1}
	 * @throws IOException
	 */
	public int writeAfterLineComment(Reader in, Writer out) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '\n' || c == '\r')
				break;
		}
		
		return c;
	}

	/**
	 * 将输入流写入输出流，直到写完块注释结束符（<code>&#47&#42...&#42&#47</code>）后停止。
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
	 * 将输入流写入输出流，直到写完引号后停止（比如：{@code "}、{@code '}）。
	 * 
	 * @param in
	 * @param out
	 * @param quoteChar
	 * @param escapeChar
	 *            转义标识符，不应为引号结束符，下一个字符是普通字符
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
	 * 将输入流写入输出流，直到写完引号后停止（比如：{@code "}、{@code '}），并将写入内容复制至{@code sb}。
	 * 
	 * @param in
	 * @param out
	 * @param quoteChar
	 * @param escapeChar
	 *            转义标识符，不应为引号结束符，下一个字符是普通字符
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
	 * 将输入流写入输出流，直到写完引号后停止（比如：{@code "}、{@code '}），连续的两个引号被认为是转义内容。
	 * 
	 * @param in
	 * @param out
	 * @param quoteChar
	 * @return {@code -1}已写完；其他 未写入输出流的下一个非引号字符
	 * @throws IOException
	 */
	public int writeAfterQuoteEscapeSelf(Reader in, Writer out, int quoteChar) throws IOException
	{
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == quoteChar)
			{
				c = in.read();

				if (c == quoteChar)
				{
					out.write(c);
				}
				else
				{
					return c;
				}
			}
		}

		return -1;
	}

	/**
	 * 将输入流写入输出流，直到写完匹配字符串后停止。
	 * 
	 * @param in
	 * @param out
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected void writeAfterString(Reader in, Writer out, char[] str) throws IOException
	{
		int c = -1;
		int matchCount = 0;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (matchCount == str.length)
				break;
			else if (c == str[matchCount])
				matchCount++;
			else
				matchCount = 0;
		}
	}

	/**
	 * 如果输入流匹配给定字符串，则读取，否则，回退输入流到读取前的位置。
	 * <p>
	 * 此方法需要{@code in}支持{@linkplain Reader#markSupported()}。
	 * </p>
	 * 
	 * @param in
	 * @param str
	 * @param start
	 * @param end
	 * @return {@code true} 匹配已读完；{@code false} 不匹配已回退
	 * @throws IOException
	 */
	protected boolean readIfMatchWithReset(Reader in, char[] str, int start, int end) throws IOException
	{
		in.mark(str.length);

		int c = -1;
		int matchCount = 0;

		while ((c = in.read()) > -1)
		{
			if (matchCount >= (end - start))
			{
				break;
			}
			else if (c == str[matchCount + start])
			{
				matchCount++;
			}
			else
			{
				in.reset();
				return false;
			}
		}

		return true;
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
