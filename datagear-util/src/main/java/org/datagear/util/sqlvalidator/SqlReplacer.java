/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
 * SQL替换器。
 * <p>
 * 此类替换SQL中的：
 * </p>
 * <p>
 * SQL字符串（{@code '...'}）、<br>
 * SQL引用标识符串（{@code 引用符...引用符}，比如：{@code "NAME"}）、
 * </p>
 * 为指定字符串。
 * 
 * @author datagear@163.com
 *
 */
public class SqlReplacer extends TextParserSupport
{
	private boolean replaceSqlString = true;

	private boolean replaceQuoteIdentifier = true;

	private String stringReplacement = "";

	private String quoteIdentifierReplacement = "";

	public SqlReplacer()
	{
		super();
	}

	public boolean isReplaceSqlString()
	{
		return replaceSqlString;
	}

	public void setReplaceSqlString(boolean replaceSqlString)
	{
		this.replaceSqlString = replaceSqlString;
	}

	public boolean isReplaceQuoteIdentifier()
	{
		return replaceQuoteIdentifier;
	}

	public void setReplaceQuoteIdentifier(boolean replaceQuoteIdentifier)
	{
		this.replaceQuoteIdentifier = replaceQuoteIdentifier;
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
	 * 替换SQL。
	 * 
	 * @param sql
	 * @param identifierQuote
	 *            允许{@code null}，数据库标识引用符
	 * @return 替换结果
	 */
	public String replace(String sql, String identifierQuote)
	{
		if (sql == null)
			return null;

		if (!this.replaceSqlString && !this.replaceSqlString)
			return sql;

		Reader in = null;
		StringWriter out = null;

		try
		{
			in = new StringReader(sql);
			out = new StringWriter(sql.length());

			replace(in, out, identifierQuote);
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
	 * @param identifierQuote
	 *            允许{@code null}，数据库标识引用符
	 * @return
	 * @throws IOException
	 */
	public void replace(Reader in, Writer out, String identifierQuote) throws IOException
	{
		NopWriter nopOut = new NopWriter();

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
			// SQL字符串
			else if (c == '\'' && this.replaceSqlString)
			{
				out.write(c);
				out.write(this.stringReplacement);
				out.write(c);

				c = writeAfterQuoteEscapeSelf(in, nopOut, '\'');
			}
			else if (c == iqs0 && this.replaceQuoteIdentifier)
			{
				// 标识引用符是单字符
				if (iqsLen == 1)
				{
					out.write(iqs0);
					out.write(this.quoteIdentifierReplacement);
					out.write(iqs0);

					c = writeAfterQuoteEscapeSelf(in, nopOut, iqs[0]);
				}
				// 标识引用符是字符串
				else if (iqsLen > 1 && readIfMatchWithReset(in, iqs, 1, iqsLen))
				{
					out.write(iqs);
					out.write(this.quoteIdentifierReplacement);
					out.write(iqs);

					writeAfterSqlIdentifierQuote(in, nopOut, iqs);
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
