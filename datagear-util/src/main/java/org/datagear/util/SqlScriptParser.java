/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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
	public static final String LINE_SEPARATOR = IOUtil.LINE_SEPARATOR;

	public static final char DEFAULT_DELIMITER_CHAR = ';';

	public static final String DEFAULT_DELIMITER = DEFAULT_DELIMITER_CHAR + "";

	public static final Pattern DELIMITER_PATTERN = Pattern
			.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE);

	public static final int SQL_SNIPPET_STRING = 2;

	public static final int SQL_SNIPPET_COMMENT_LINE = 4;

	public static final int SQL_SNIPPET_COMMENT_BLOCK = 8;

	public static final int SQL_SNIPPET_NONE = 1024;

	private Reader sqlScriptReader;

	/** SQL脚本的上下文起始行 */
	private int contextStartRow = 0;

	/** SQL脚本的上下文起始行中的起始列 */
	private int contextStartColumn = 0;

	/** 语句分隔符 */
	private String delimiter = DEFAULT_DELIMITER;

	/** 缓冲SQL输入流 */
	private BufferedReader _bufferedSqlScriptReader;
	private boolean _readFinish = false;

	/** 解析过程：当前行 */
	protected int _currentRow = 0;
	/** 解析过程：当前SQL的起始行（包含） */
	protected int _currentSqlStartRow = 0;
	/** 解析过程：当前SQL的起始行中的起始列（包含） */
	protected int _currentSqlStartColumn = 0;
	/** 解析过程：当前SQL的结束行号（包含） */
	protected int _currentSqlEndRow = 0;
	/** 解析过程：当前SQL的结束行中的结束列（不包含） */
	protected int _currentSqlEndColumn = 0;
	/** 当前SQL片段 */
	protected Stack<Integer> _currentSqlSnippets = new Stack<Integer>();

	/** 逐个解析：当前SQL语句存储器 */
	private StringBuilder _sqlStatementBuilder = new StringBuilder();
	/** 逐个解析：下一行解析的SQL语句列表 */
	private List<SqlStatement> _nextLineSqlStatements = new ArrayList<SqlStatement>(2);
	/** 逐个解析：读取的下一行SQL语句列表索引 */
	private int _nextLineSqlStatementsGotIndex = 0;

	public SqlScriptParser()
	{
		super();
	}

	public SqlScriptParser(Reader sqlScriptReader)
	{
		super();
		this.sqlScriptReader = sqlScriptReader;
		this._bufferedSqlScriptReader = getSqlScriptBufferedReader();
	}

	public Reader getSqlScriptReader()
	{
		return sqlScriptReader;
	}

	public void setSqlScriptReader(Reader sqlScriptReader)
	{
		this.sqlScriptReader = sqlScriptReader;
		this._bufferedSqlScriptReader = getSqlScriptBufferedReader();
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
		this._currentSqlEndColumn = contextStartColumn;
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
	 * 解析全部SQL语句。
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<SqlStatement> parseAll() throws IOException
	{
		List<SqlStatement> sqlStatements = new LinkedList<SqlStatement>();

		BufferedReader bufferedReader = this._bufferedSqlScriptReader;

		String line = null;
		StringBuilder sqlBuilder = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null)
			handleLine(sqlStatements, sqlBuilder, line);

		addSqlStatement(sqlStatements, sqlBuilder);

		this._readFinish = true;

		return sqlStatements;
	}

	/**
	 * 解析下一个SQL语句。
	 * <p>
	 * 返回{@code null}表示已解析完成。
	 * </p>
	 * 
	 * @return
	 * @throws IOException
	 */
	public SqlStatement parseNext() throws IOException
	{
		if (this._nextLineSqlStatementsGotIndex < this._nextLineSqlStatements.size())
			return this._nextLineSqlStatements.get(this._nextLineSqlStatementsGotIndex++);

		this._nextLineSqlStatements.clear();
		this._nextLineSqlStatementsGotIndex = 0;

		while (this._nextLineSqlStatements.size() < 1 && parseNextLine())
			;

		return (this._nextLineSqlStatements.size() > 0
				? this._nextLineSqlStatements.get(this._nextLineSqlStatementsGotIndex++)
				: null);
	}

	/**
	 * 解析下一行。
	 * 
	 * @return {@code false} 已解析完最后一行；{@code true} 未解析完
	 * @throws IOException
	 */
	protected boolean parseNextLine() throws IOException
	{
		if (this._readFinish)
			return false;

		String line = this._bufferedSqlScriptReader.readLine();

		if (line != null)
			handleLine(this._nextLineSqlStatements, this._sqlStatementBuilder, line);
		else
		{
			this._readFinish = true;
			addSqlStatement(this._nextLineSqlStatements, this._sqlStatementBuilder);
		}

		return !this._readFinish;
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

		boolean findDelimiter = true;

		String trimmedLine = line.trim();

		// 空行
		if (trimmedLine.isEmpty())
		{
			if (!isSqlBuilderEmpty)
			{
				sqlBuilder.append(line);
				sqlBuilder.append(LINE_SEPARATOR);
			}

			findDelimiter = false;
		}

		boolean isCommentLine = isCommentLine(trimmedLine);

		if (isCommentLine)
		{
			Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);

			// 分隔符声明行
			if (matcher.find())
			{
				this.delimiter = matcher.group(5);

				if (!isSqlBuilderEmpty)
					sqlBuilder.append(LINE_SEPARATOR);

				findDelimiter = false;
			}
		}

		if (findDelimiter)
		{
			if (isSqlBuilderEmpty)
				_currentSqlStartRow = _currentRow;

			int handleIndex = 0;
			int lineLength = line.length();

			while (handleIndex < lineLength)
			{
				if (isSqlBuilderEmpty)
				{
					_currentSqlStartRow = _currentRow;
					_currentSqlStartColumn = handleIndex;
				}

				int delimiterIndex = findNextDelimiterIndex(sqlBuilder, line, handleIndex);

				// 没有分隔符
				if (delimiterIndex < 0)
				{
					// 忽略无用注释行
					if (isCommentLine && isSqlBuilderEmpty)
						;
					else
					{
						sqlBuilder.append(line.substring(handleIndex));
						sqlBuilder.append(LINE_SEPARATOR);

						isSqlBuilderEmpty = false;
					}

					_currentSqlEndRow = _currentRow;
					_currentSqlEndColumn = lineLength;

					handleIndex = lineLength;
				}
				else
				{
					if (delimiterIndex > 0)
					{
						_currentSqlEndRow = _currentRow;
						_currentSqlEndColumn = delimiterIndex;

						String scriptBefore = line.substring(handleIndex, delimiterIndex);
						sqlBuilder.append(scriptBefore);
					}

					addSqlStatement(sqlStatements, sqlBuilder);
					clear(sqlBuilder);

					isSqlBuilderEmpty = true;
					handleIndex = delimiterIndex + this.delimiter.length();
				}
			}
		}

		_currentRow++;
	}

	/**
	 * 在SQL行中查找下一个分隔符位置。
	 * <p>
	 * 返回{@code <0}表示没有分隔符。
	 * </p>
	 * 
	 * @param sqlBuilder
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findNextDelimiterIndex(StringBuilder sqlBuilder, String line, int startIndex)
	{
		if (DEFAULT_DELIMITER.equals(this.delimiter))
		{
			return findNextDelimiterIndexForDefaultDelimiter(sqlBuilder, line, startIndex);
		}
		else
		{
			return line.indexOf(this.delimiter, startIndex);
		}
	}

	/**
	 * 当使用默认分隔符时（{@linkplain #DEFAULT_DELIMITER}），在SQL行中查找下一个分隔符位置。
	 * <p>
	 * 返回{@code <0}表示没有分隔符。
	 * </p>
	 * 
	 * @param sqlBuilder
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findNextDelimiterIndexForDefaultDelimiter(StringBuilder sqlBuilder, String line, int startIndex)
	{
		int lineLength = line.length();

		if (startIndex >= lineLength)
			return -1;

		int sqlSnippetType = getCurrentSqlSnippetType();

		if (sqlSnippetType != SQL_SNIPPET_NONE)
		{
			int endIndex = -1;

			if (sqlSnippetType == SQL_SNIPPET_STRING)
				endIndex = findSqlStringEndIndex(line, startIndex);
			else if (sqlSnippetType == SQL_SNIPPET_COMMENT_LINE)
				endIndex = findSqlCommentLineEndIndex(line, startIndex);
			else if (sqlSnippetType == SQL_SNIPPET_COMMENT_BLOCK)
				endIndex = findSqlCommentBlockEndIndex(line, startIndex);

			if (endIndex >= 0)
			{
				removeCurrentSqlSnippetType();

				return findNextDelimiterIndexForDefaultDelimiter(sqlBuilder, line, endIndex);
			}
			else
				return -1;
		}

		for (int i = startIndex; i < lineLength;)
		{
			char c = line.charAt(i);

			if (c == '\'')
			{
				i = findSqlStringEndIndex(line, i + 1);

				if (i < 0)
				{
					i = lineLength;
					setCurrentSqlSnippet(SQL_SNIPPET_STRING);
				}
			}
			else if (c == '-')
			{
				char cn = (i + 1 >= lineLength ? 0 : line.charAt(i + 1));

				if (cn == '-')
				{
					i = findSqlCommentLineEndIndex(line, i + 2);

					if (i < 0)
					{
						i = lineLength;
						setCurrentSqlSnippet(SQL_SNIPPET_COMMENT_LINE);
					}
				}
				else
					i += 1;
			}
			else if (c == '/')
			{
				char cn = (i + 1 >= lineLength ? 0 : line.charAt(i + 1));

				if (cn == '*')
				{
					i = findSqlCommentBlockEndIndex(line, i + 2);

					if (i < 0)
					{
						i = lineLength;
						setCurrentSqlSnippet(SQL_SNIPPET_COMMENT_BLOCK);
					}
				}
				else
					i += 1;
			}
			else if (c == DEFAULT_DELIMITER_CHAR)
			{
				return i;
			}
			else
				i += 1;
		}

		return -1;
	}

	/**
	 * 设置当前SQL片段类型。
	 * 
	 * @param sqlSnippet
	 */
	protected void setCurrentSqlSnippet(int sqlSnippet)
	{
		this._currentSqlSnippets.push(sqlSnippet);
	}

	/**
	 * 获取当前SQL片段类型。
	 * <p>
	 * 如果没有，则返回{@linkplain #SQL_SNIPPET_NONE}。
	 * </p>
	 * 
	 * @return
	 */
	protected int getCurrentSqlSnippetType()
	{
		if (this._currentSqlSnippets.isEmpty())
			return SQL_SNIPPET_NONE;

		return this._currentSqlSnippets.peek();
	}

	/**
	 * 移除当前SQL片段类型。
	 * <p>
	 * 如果没有，则返回{@linkplain #SQL_SNIPPET_NONE}。
	 * </p>
	 * 
	 * @return
	 */
	protected int removeCurrentSqlSnippetType()
	{
		if (this._currentSqlSnippets.isEmpty())
			return SQL_SNIPPET_NONE;

		return this._currentSqlSnippets.pop();
	}

	/**
	 * 清除SQL片段类型。
	 */
	protected void clearCurrentSqlSnippetType()
	{
		this._currentSqlSnippets.clear();
	}

	/**
	 * 查找SQL字符串结束位置（结束单引号的下一个位置）。
	 * <p>
	 * 返回{@code <0}表示没有结束。
	 * </p>
	 * 
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findSqlStringEndIndex(String line, int startIndex)
	{
		int length = line.length();

		for (int i = startIndex; i < length; i++)
		{
			char c = line.charAt(i);

			if (c == '\'')
			{
				char cn = (i + 1 >= length ? 0 : line.charAt(i + 1));

				if (cn == '\'')
					i += 1;
				else
					return i + 1;
			}
		}

		return -1;
	}

	/**
	 * 查找SQL行注释结束位置（换行符的下一个位置）。
	 * <p>
	 * 返回{@code <0}表示没有结束。
	 * </p>
	 * 
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findSqlCommentLineEndIndex(String line, int startIndex)
	{
		return line.length();
	}

	/**
	 * 查找SQL块注释结束位置（“*&#47”的下一个位置）。
	 * <p>
	 * 返回{@code <0}表示没有结束。
	 * </p>
	 * 
	 * @param line
	 * @param startIndex
	 * @return
	 */
	protected int findSqlCommentBlockEndIndex(String line, int startIndex)
	{
		int length = line.length();

		for (int i = startIndex; i < length; i++)
		{
			char c = line.charAt(i);

			if (c == '*')
			{
				char cn = (i + 1 >= length ? 0 : line.charAt(i + 1));

				if (cn == '/')
					return i + 1;
			}
		}

		return -1;
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
		if (!sql.isEmpty() && !isAsteriskComment(sql))
		{
			SqlStatement sqlStatement = new SqlStatement(sql, _currentSqlStartRow, _currentSqlStartColumn,
					_currentSqlEndRow, _currentSqlEndColumn);

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
	protected boolean isAsteriskComment(String trimmedString)
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
		return trimmedLine.startsWith("--") || trimmedLine.startsWith("//");
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
	 * 清空{@linkplain StringBuilder}。
	 * 
	 * @param stringBuilder
	 */
	protected void clear(StringBuilder stringBuilder)
	{
		int len = stringBuilder.length();

		if (len == 0)
			return;

		stringBuilder.delete(0, len);
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
