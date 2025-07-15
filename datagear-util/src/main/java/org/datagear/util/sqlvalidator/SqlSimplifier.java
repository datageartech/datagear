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

package org.datagear.util.sqlvalidator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.TextParserSupport;

/**
 * SQL简化器。
 * <p>
 * 此类删除SQL中的单行注释内容、多行注释内容：
 * </p>
 * <p>
 * {@code --注释}
 * </p>
 * <p>
 * <code>&#47*注释*&#47 </code>
 * </p>
 * <p>
 * 可选地（默认处理），将：
 * </p>
 * <p>
 * SQL字符串（{@code '...'}）替换为空字符串（{@code ''}）；
 * </p>
 * <p>
 * SQL引用标识符串（比如：{@code "NAME"}）替换为空标识符串（比如：{@code ""}）
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlSimplifier extends TextParserSupport
{
	/** 引用标识符 */
	private String identifierQuote;

	/** 是否处理SQL字符串 */
	private boolean handleString = true;

	/** 是否处理引用标识符 */
	private boolean handleQuoteIdentifier = true;

	/** SQL字符串替换目标 */
	private String stringReplacement = "''";

	/** 引用标识符替换目标 */
	private String quoteIdentifierReplacement = "";

	public SqlSimplifier()
	{
		super();
	}

	public SqlSimplifier(String identifierQuote)
	{
		super();
		this.identifierQuote = identifierQuote;
		this.quoteIdentifierReplacement = this.identifierQuote + this.identifierQuote;
	}

	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	public boolean isHandleString()
	{
		return handleString;
	}

	public void setHandleString(boolean handleString)
	{
		this.handleString = handleString;
	}

	public boolean isHandleQuoteIdentifier()
	{
		return handleQuoteIdentifier;
	}

	public void setHandleQuoteIdentifier(boolean handleQuoteIdentifier)
	{
		this.handleQuoteIdentifier = handleQuoteIdentifier;
	}

	public String getStringReplacement()
	{
		return stringReplacement;
	}

	public void setStringReplacement(String stringReplacement)
	{
		this.stringReplacement = stringReplacement;
	}

	public String getQuoteIdentifierReplacement()
	{
		return quoteIdentifierReplacement;
	}

	public void setQuoteIdentifierReplacement(String quoteIdentifierReplacement)
	{
		this.quoteIdentifierReplacement = quoteIdentifierReplacement;
	}

	/**
	 * 简化SQL。
	 * 
	 * @param sql
	 *            允许{@code null}
	 * @return 简化结果
	 */
	public String simplify(String sql)
	{
		if (sql == null)
			return null;

		Reader in = null;
		StringWriter out = null;

		try
		{
			in = new StringReader(sql);
			out = new StringWriter(sql.length());

			simplify(in, out);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}

		return out.toString();
	}

	/**
	 * 替换SQL。
	 * 
	 * @param in
	 *            SQL输入流，需支持{@linkplain Reader#markSupported()}
	 * @param out
	 *            替换结果输出流
	 * @return
	 * @throws IOException
	 */
	public void simplify(Reader in, Writer out) throws IOException
	{
		NopWriter nopOut = new NopWriter();

		char[] iqs = (StringUtil.isEmpty(this.identifierQuote) ? null : this.identifierQuote.toCharArray());
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
			// 行注释
			else if (c == '-')
			{
				int cn = in.read();

				if (cn == '-')
				{
					c = writeAfterLineComment(in, nopOut);
				}
				else
				{
					out.write(c);
					c = cn;
				}
			}
			// 块注释
			else if (c == '/')
			{
				int cn = in.read();

				if (cn == '*')
				{
					writeAfterBlockComment(in, nopOut);
					c = -99;
				}
				else
				{
					out.write(c);
					c = cn;
				}
			}
			// SQL字符串
			else if (c == '\'')
			{
				if (this.handleString)
				{
					out.write(this.stringReplacement);
					c = writeAfterQuoteEscapeSelf(in, nopOut, '\'');
				}
				else
				{
					out.write(c);
					c = writeAfterQuoteEscapeSelf(in, out, '\'');
				}
			}
			// 引用标识符
			else if (c == iqs0)
			{
				// 标识引用符是单字符
				if (iqsLen == 1)
				{
					if (this.handleQuoteIdentifier)
					{
						out.write(this.quoteIdentifierReplacement);
						c = writeAfterQuoteEscapeSelf(in, nopOut, iqs[0]);
					}
					else
					{
						out.write(c);
						c = writeAfterQuoteEscapeSelf(in, out, iqs[0]);
					}
				}
				// 标识引用符是字符串
				else if (iqsLen > 1 && readIfMatchWithReset(in, iqs, 1, iqsLen))
				{
					if (this.handleQuoteIdentifier)
					{
						out.write(this.quoteIdentifierReplacement);
						writeAfterSqlIdentifierQuote(in, nopOut, iqs);
						c = -99;
					}
					else
					{
						out.write(this.identifierQuote);
						writeAfterSqlIdentifierQuote(in, out, iqs);
						c = -99;
					}
				}
				else
				{
					out.write(c);
					c = -99;
				}
			}
			else
			{
				out.write(c);
				c = -99;
			}
		}
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

	protected static class NopWriter extends Writer
	{
		public NopWriter()
		{
			super();
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException
		{
		}

		@Override
		public void flush() throws IOException
		{
		}

		@Override
		public void close() throws IOException
		{
		}
	}
}
