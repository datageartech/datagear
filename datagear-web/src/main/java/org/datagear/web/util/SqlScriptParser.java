/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL脚本解析器。
 * 
 * @author datagear@163.com
 *
 */
public class SqlScriptParser
{
	protected static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	protected static final String DEFAULT_DELIMITER = ";";

	protected static final Pattern DELIMITER_PATTERN = Pattern
			.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE);

	private Reader sqlScriptReader;

	/** SQL脚本的上下文起始行号 */
	private int contextStartLine = 1;

	/** SQL脚本的上下文起始行的偏移量 */
	private int contextStartOffset = 0;

	/** 语句分隔符 */
	private String delimiter = DEFAULT_DELIMITER;

	public SqlScriptParser()
	{
		super();
	}

	public SqlScriptParser(Reader sqlScriptReader)
	{
		super();
		this.sqlScriptReader = sqlScriptReader;
	}

	public Reader getSqlScriptReader()
	{
		return sqlScriptReader;
	}

	public void setSqlScriptReader(Reader sqlScriptReader)
	{
		this.sqlScriptReader = sqlScriptReader;
	}

	public int getContextStartLine()
	{
		return contextStartLine;
	}

	public void setContextStartLine(int contextStartLine)
	{
		this.contextStartLine = contextStartLine;
	}

	public int getContextStartOffset()
	{
		return contextStartOffset;
	}

	public void setContextStartOffset(int contextStartOffset)
	{
		this.contextStartOffset = contextStartOffset;
	}

	/**
	 * 解析。
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<SqlStatement> parse() throws IOException
	{
		return doParse();
	}

	/**
	 * 执行解析。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected List<SqlStatement> doParse() throws IOException
	{
		List<SqlStatement> sqlStatements = new LinkedList<SqlStatement>();

		BufferedReader bufferedReader = getSqlScriptBufferedReader();

		String line = null;
		int lineNumber = this.contextStartLine;
		StringBuilder sqlScript = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null)
		{
			String trimmedLine = line.trim();

			// 空行
			if (trimmedLine.isEmpty())
			{
				sqlScript.append(line);
				sqlScript.append(LINE_SEPARATOR);
			}
			// 注释行
			else if (isCommentLine(trimmedLine))
			{
				Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);
				if (matcher.find())
					this.delimiter = matcher.group(5);

				sqlScript.append(LINE_SEPARATOR);
			}
			else
			{

			}

			lineNumber++;
		}

		return sqlStatements;
	}

	/**
	 * 是否是注释行。
	 * 
	 * @param trimmedLine
	 * @return
	 */
	protected boolean isCommentLine(String trimmedLine)
	{
		return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
	}

	/**
	 * 获取SQL脚本输入流的{@linkplain BufferedReader}。
	 * 
	 * @return
	 */
	protected BufferedReader getSqlScriptBufferedReader()
	{
		BufferedReader bufferedReader = null;

		if (this.sqlScriptReader instanceof BufferedReader)
			bufferedReader = (BufferedReader) this.sqlScriptReader;
		else
			bufferedReader = new BufferedReader(this.sqlScriptReader);

		return bufferedReader;
	}

	/**
	 * SQL语句解析结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SqlStatement implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 解析的SQL语句 */
		private String sql;

		/** SQL语句起始行 */
		private int startLine;

		/** SQL语句起始行偏移量 */
		private int startOffset;

		/** SQL语句结束行 */
		private int finishLine;

		/** SQL语句结束行偏移量 */
		private int finishOffset;

		public SqlStatement()
		{
			super();
		}

		public SqlStatement(String sql, int startLine, int startOffset, int finishLine, int finishOffset)
		{
			super();
			this.sql = sql;
			this.startLine = startLine;
			this.startOffset = startOffset;
			this.finishLine = finishLine;
			this.finishOffset = finishOffset;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public int getStartLine()
		{
			return startLine;
		}

		public void setStartLine(int startLine)
		{
			this.startLine = startLine;
		}

		public int getStartOffset()
		{
			return startOffset;
		}

		public void setStartOffset(int startOffset)
		{
			this.startOffset = startOffset;
		}

		public int getFinishLine()
		{
			return finishLine;
		}

		public void setFinishLine(int finishLine)
		{
			this.finishLine = finishLine;
		}

		public int getFinishOffset()
		{
			return finishOffset;
		}

		public void setFinishOffset(int finishOffset)
		{
			this.finishOffset = finishOffset;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [sql=" + sql + ", startLine=" + startLine + ", startOffset="
					+ startOffset + ", finishLine=" + finishLine + ", finishOffset=" + finishOffset + "]";
		}
	}
}
