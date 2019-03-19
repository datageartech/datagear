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
	public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	public static final String DEFAULT_DELIMITER = ";";

	public static final Pattern DELIMITER_PATTERN = Pattern
			.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE);

	private Reader sqlScriptReader;

	/** SQL脚本的上下文起始行 */
	private int contextStartRow = 0;

	/** SQL脚本的上下文起始行中的起始列 */
	private int contextStartColumn = 0;

	/** 语句分隔符 */
	private String delimiter = DEFAULT_DELIMITER;

	/** 解析过程：当前行 */
	protected int _currentRow = 0;
	/** 解析过程：当前SQL的起始行（包含） */
	protected int _currentSqlStartRow = 0;
	/** 解析过程：当前SQL的起始行中的起始列（包含） */
	protected int _currentSqlStartColumn = 0;
	/** 解析过程：当前SQL的结束行号（包含） */
	protected int _currentSqlEndRow = 0;
	/** 解析过程：当前SQL的结束行中的结束列（不包含） */
	protected int _currentsqlEndColumn = 0;

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

	public int getContextStartRow()
	{
		return contextStartRow;
	}

	public void setContextStartRow(int contextStartRow)
	{
		this.contextStartRow = contextStartRow;

		this._currentRow = contextStartRow;
		this._currentSqlStartRow = contextStartRow;
		this._currentSqlEndRow = contextStartColumn;
	}

	public int getContextStartColumn()
	{
		return contextStartColumn;
	}

	public void setContextStartColumn(int contextStartColumn)
	{
		this.contextStartColumn = contextStartColumn;

		this._currentSqlStartColumn = contextStartColumn;
		this._currentsqlEndColumn = contextStartColumn;
	}

	public String getDelimiter()
	{
		return delimiter;
	}

	public void setDelimiter(String delimiter)
	{
		this.delimiter = delimiter;
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
		StringBuilder sqlBuilder = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null)
			handleLine(sqlStatements, sqlBuilder, line);

		addSqlStatement(sqlStatements, sqlBuilder);

		return sqlStatements;
	}

	/**
	 * 处理一行SQL内容。
	 * 
	 * @param sqlStatements
	 * @param sqlBuilder
	 * @param line
	 */
	protected void handleLine(List<SqlStatement> sqlStatements, StringBuilder sqlBuilder, String line)
	{
		boolean isSqlBuilderEmpty = isEmpty(sqlBuilder);

		String trimmedLine = line.trim();

		// 空行
		if (trimmedLine.isEmpty())
		{
			if (!isSqlBuilderEmpty)
			{
				sqlBuilder.append(line);
				sqlBuilder.append(LINE_SEPARATOR);
			}
		}
		// 注释行
		else if (isCommentLine(trimmedLine))
		{
			Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);
			if (matcher.find())
				this.delimiter = matcher.group(5);

			if (!isSqlBuilderEmpty)
			{
				sqlBuilder.append(LINE_SEPARATOR);
			}
		}
		else
		{
			if (isSqlBuilderEmpty)
				_currentSqlStartRow = _currentRow;

			int handleIndex = 0;
			int lineLength = line.length();

			while (handleIndex < lineLength)
			{
				if (isEmpty(sqlBuilder))
					_currentSqlStartColumn = handleIndex;

				int delimiterIndex = findNextDelimiterIndex(sqlBuilder, line, handleIndex);

				// 没有分隔符
				if (delimiterIndex < 0)
				{
					if (handleIndex == 0)
						sqlBuilder.append(line);
					else
						sqlBuilder.append(line.substring(handleIndex));
					sqlBuilder.append(LINE_SEPARATOR);

					_currentSqlEndRow = _currentRow;
					_currentsqlEndColumn = lineLength;

					handleIndex = lineLength;
				}
				else
				{
					_currentSqlEndRow = _currentRow;
					_currentsqlEndColumn = delimiterIndex + delimiter.length();

					String scriptBefore = line.substring(handleIndex, delimiterIndex);
					sqlBuilder.append(scriptBefore);

					addSqlStatement(sqlStatements, sqlBuilder);

					sqlBuilder.delete(0, sqlBuilder.length());

					handleIndex = _currentsqlEndColumn;
				}
			}
		}

		_currentRow++;
	}

	/**
	 * 在SQL行中查找下一个分隔符位置，没有返回{@code -1}。
	 * 
	 * @param sqlBuilder
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findNextDelimiterIndex(StringBuilder sqlBuilder, String line, int startIndex)
	{
		int delimiterIndex = -1;

		if (DEFAULT_DELIMITER.equals(this.delimiter))
		{
			// TODO 特殊处理SQL字符串、函数、存储过程，因为它们中间可能包含';'字符
			delimiterIndex = line.indexOf(this.delimiter, startIndex);
		}
		else
		{
			delimiterIndex = line.indexOf(this.delimiter, startIndex);
		}

		return delimiterIndex;
	}

	/**
	 * 从{@code sqlBuilder}构建{@linkplain SqlStatement}，并添加至{@code sqlStatements}。
	 * 
	 * @param sqlStatements
	 * @param sqlBuilder
	 * @return true 已构建；false 不能构建
	 */
	protected boolean addSqlStatement(List<SqlStatement> sqlStatements, StringBuilder sqlBuilder)
	{
		String sql = sqlBuilder.toString().trim();
		if (!sql.isEmpty() && !isAsteriskPairComment(sql))
		{
			SqlStatement sqlStatement = new SqlStatement(sql, _currentSqlStartRow, _currentSqlStartColumn,
					_currentSqlEndRow, _currentsqlEndColumn);

			sqlStatements.add(sqlStatement);

			return true;
		}

		return false;
	}

	/**
	 * 判断字符串是否是"&#47*...*&#47"注释。
	 * 
	 * @param trimmedString
	 * @return
	 */
	protected boolean isAsteriskPairComment(String trimmedString)
	{
		return trimmedString.startsWith("/*") && trimmedString.endsWith("*/");
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
	 * {@linkplain StringBuilder}是否为空。
	 * 
	 * @param stringBuilder
	 * @return
	 */
	protected boolean isEmpty(StringBuilder stringBuilder)
	{
		return (stringBuilder.length() == 0);
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

		/** SQL语句起始行（包含，以0开始） */
		private int startRow;

		/** SQL语句起始行中的起始列（包含，以0开始） */
		private int startColumn;

		/** SQL语句结束行（包含） */
		private int endRow;

		/** SQL语句结束行中的结束列（不包含） */
		private int endColumn;

		public SqlStatement()
		{
			super();
		}

		public SqlStatement(String sql, int startRow, int startColumn, int endRow, int endColumn)
		{
			super();
			this.sql = sql;
			this.startRow = startRow;
			this.startColumn = startColumn;
			this.endRow = endRow;
			this.endColumn = endColumn;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public int getStartRow()
		{
			return startRow;
		}

		public void setStartRow(int startRow)
		{
			this.startRow = startRow;
		}

		public int getStartColumn()
		{
			return startColumn;
		}

		public void setStartColumn(int startColumn)
		{
			this.startColumn = startColumn;
		}

		public int getEndRow()
		{
			return endRow;
		}

		public void setEndRow(int endRow)
		{
			this.endRow = endRow;
		}

		public int getEndColumn()
		{
			return endColumn;
		}

		public void setEndColumn(int endColumn)
		{
			this.endColumn = endColumn;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [sql=" + sql + ", startRow=" + startRow + ", startColumn="
					+ startColumn + ", endRow=" + endRow + ", endColumn=" + endColumn + "]";
		}
	}
}
