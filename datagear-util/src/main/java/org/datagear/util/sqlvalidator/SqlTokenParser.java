/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.sqlvalidator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.TextParserSupport;

/**
 * SQL单词解析器。
 * <p>
 * 此类以空格（空格、换行、制表符等）、逗号（{@code ,}）作为单词分隔符、解析：
 * </p>
 * <p>
 * SQL关键字、标识符、<br>
 * SQL字符串（{@code '...'}）、<br>
 * SQL引用标识符串（{@code 引用符...引用符}）、<br>
 * 行注释（<code>&#47&#47...</code>、<code>--...</code>）、<br>
 * 块注释（<code>&#47&#42...&#42&#47</code>）
 * </p>
 * 
 * @author datagear@163.com
 * @deprecated SQL校验改为采用{@linkplain SqlReplacer}策略，此类没有使用
 */
@Deprecated
public class SqlTokenParser extends TextParserSupport
{
	public SqlTokenParser()
	{
		super();
	}

	/**
	 * 解析SQL单词。
	 * 
	 * @param sql
	 * @param identifierQuote
	 *            允许{@code null}，数据库标识引用符
	 * @return
	 */
	public List<SqlToken> parse(String sql, String identifierQuote)
	{
		if (StringUtil.isEmpty(sql))
			return Collections.emptyList();

		Reader in = null;

		try
		{
			in = new StringReader(sql);
			return parse(in, identifierQuote);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	/**
	 * 解析SQL单词。
	 * 
	 * @param in
	 *            SQL输入流，需支持{@linkplain Reader#markSupported()}
	 * @param identifierQuote
	 *            允许{@code null}，数据库标识引用符
	 * @return
	 * @throws IOException
	 */
	public List<SqlToken> parse(Reader in, String identifierQuote) throws IOException
	{
		List<SqlToken> tokens = new ArrayList<SqlToken>();
		
		StringWriter out = new StringWriter();
		char[] iqs = (StringUtil.isEmpty(identifierQuote) ? null : identifierQuote.toCharArray());
		int iqsLen = (iqs == null ? -1 : iqs.length);
		int iqs0 = (iqs == null ? -999 : iqs[0]);

		int c = -99;

		while (true)
		{
			if (c == -99)
				c = in.read();

			if (c == -1)
			{
				break;
			}
			else if (isWhitespace(c) || c == ',')
			{
				if(out.getBuffer().length() > 0)
				{
					addSqlToken(tokens, out, SqlToken.TYPE_OTHER);
					out = new StringWriter();
				}

				if (c == ',')
				{
					addSqlToken(tokens, ",", SqlToken.TYPE_COMMA);
					out = new StringWriter();
				}

				c = -99;
			}
			// SQL字符串
			else if (c == '\'')
			{
				out.write(c);
				c = writeAfterQuoteEscapeSelf(in, out, '\'');

				addSqlToken(tokens, out, SqlToken.TYPE_STRING);
				out = new StringWriter();
			}
			else if (c == '/')
			{
				out.write(c);
				c = in.read();

				// 行注释
				if (c == '/')
				{
					out.write(c);
					writeAfterLineComment(in, out);

					addSqlToken(tokens, out, SqlToken.TYPE_DBS_LINE_COMMENT);
					out = new StringWriter();

					c = -99;
				}
				// 块注释
				else if (c == '*')
				{
					out.write(c);
					writeAfterBlockComment(in, out);

					addSqlToken(tokens, out, SqlToken.TYPE_BLOCK_COMMENT);
					out = new StringWriter();

					c = -99;
				}
			}
			else if (c == '-')
			{
				out.write(c);
				c = in.read();

				// 行注释
				if (c == '-')
				{
					out.write(c);
					writeAfterLineComment(in, out);

					addSqlToken(tokens, out, SqlToken.TYPE_DML_LINE_COMMENT);
					out = new StringWriter();

					c = -99;
				}
			}
			else if (c == iqs0)
			{
				// 标识引用符是单字符
				if (iqsLen == 1)
				{
					out.write(c);
					c = writeAfterQuoteEscapeSelf(in, out, iqs[0]);

					addSqlToken(tokens, out, SqlToken.TYPE_QUOTE_IDENTIFIER);
					out = new StringWriter();
				}
				// 标识引用符是字符串
				else if (iqsLen > 1 && readIfMatchWithReset(in, iqs, 1, iqsLen))
				{
					out.write(iqs);
					writeAfterSqlIdentifierQuote(in, out, iqs);

					addSqlToken(tokens, out, SqlToken.TYPE_QUOTE_IDENTIFIER);
					out = new StringWriter();

					c = -99;
				}
			}
			else
			{
				out.write(c);
				c = -99;
			}
		}

		if (out.getBuffer().length() > 0)
			addSqlToken(tokens, out, SqlToken.TYPE_OTHER);

		return tokens;
	}

	/**
	 * 将输入流写入输出流，直到写完SQL标识引用符后停止（例如：{@code "..."}），连续的两个标识引用符（{@code ''}）是转义。
	 * 
	 * @param in
	 * @param out
	 * @param identifierQuote
	 *            不应为空字符串
	 * @throws IOException
	 */
	protected void writeAfterSqlIdentifierQuote(Reader in, Writer out, char[] identifierQuote) throws IOException
	{
		writeAfterString(in, out, identifierQuote);

		// 连续两个标识符是转义
		while (readIfMatchWithReset(in, identifierQuote, 0, identifierQuote.length))
			writeAfterString(in, out, identifierQuote);
	}

	protected void addSqlToken(List<SqlToken> list, String token, String type)
	{
		list.add(new SqlToken(type, token));
	}

	protected void addSqlToken(List<SqlToken> list, StringWriter out, String type)
	{
		list.add(toSqlToken(out, type));
	}

	protected SqlToken toSqlToken(StringWriter out, String type)
	{
		return new SqlToken(type, out.toString());
	}
}
