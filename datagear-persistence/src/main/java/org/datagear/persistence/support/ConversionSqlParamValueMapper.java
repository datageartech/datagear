/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapper;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.persistence.support.expression.NameExpression;
import org.datagear.persistence.support.expression.SqlExpressionErrorException;
import org.datagear.persistence.support.expression.SqlExpressionResolver;
import org.datagear.persistence.support.expression.SqlExpressionSyntaxErrorException;
import org.datagear.persistence.support.expression.VariableExpressionErrorException;
import org.datagear.persistence.support.expression.VariableExpressionResolver;
import org.datagear.persistence.support.expression.VariableExpressionSyntaxErrorException;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlParamValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 支持类型转换的{@linkplain SqlParamValueMapper}。
 * <p>
 * 对于SQL大对象类型、二进制类型，此类支持原值是文件路径格式的字符串，具体参考{@linkplain FilePathValueResolver}。
 * </p>
 * <p>
 * 如果{@linkplain #hasExpressionEvaluationContext()}，它还支持变量表达式<code>"#{...}"</code>、
 * SQL表达式<code>"${select...}"</code>。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConversionSqlParamValueMapper extends AbstractSqlParamValueMapper
{
	protected static final VariableExpressionResolver DEFAULT_VARIABLE_EXPRESSION_RESOLVER = new VariableExpressionResolver();

	protected static final SqlExpressionResolver DEFAULT_SQL_EXPRESSION_RESOLVER = new SqlExpressionResolver();

	protected static final SpelExpressionParser DEFAULT_SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

	protected static final FilePathValueResolver DEFAULT_FILE_PATH_VALUE_RESOLVER = new FilePathValueResolver();

	/** 变量表达式解析器 */
	private VariableExpressionResolver variableExpressionResolver = DEFAULT_VARIABLE_EXPRESSION_RESOLVER;

	/** SQL表达式解析器 */
	private SqlExpressionResolver sqlExpressionResolver = DEFAULT_SQL_EXPRESSION_RESOLVER;

	/** 变量表达式计算器 */
	private SpelExpressionParser spelExpressionParser = DEFAULT_SPEL_EXPRESSION_PARSER;

	private FilePathValueResolver filePathValueResolver = DEFAULT_FILE_PATH_VALUE_RESOLVER;

	/** 表达式计算上下文 */
	private ExpressionEvaluationContext expressionEvaluationContext = null;

	public ConversionSqlParamValueMapper()
	{
		super();
	}

	public VariableExpressionResolver getVariableExpressionResolver()
	{
		return variableExpressionResolver;
	}

	public void setVariableExpressionResolver(VariableExpressionResolver variableExpressionResolver)
	{
		this.variableExpressionResolver = variableExpressionResolver;
	}

	public SqlExpressionResolver getSqlExpressionResolver()
	{
		return sqlExpressionResolver;
	}

	public void setSqlExpressionResolver(SqlExpressionResolver sqlExpressionResolver)
	{
		this.sqlExpressionResolver = sqlExpressionResolver;
	}

	public SpelExpressionParser getSpelExpressionParser()
	{
		return spelExpressionParser;
	}

	public void setSpelExpressionParser(SpelExpressionParser spelExpressionParser)
	{
		this.spelExpressionParser = spelExpressionParser;
	}

	public FilePathValueResolver getFilePathValueResolver()
	{
		return filePathValueResolver;
	}

	public void setFilePathValueResolver(FilePathValueResolver filePathValueResolver)
	{
		this.filePathValueResolver = filePathValueResolver;
	}

	public boolean hasExpressionEvaluationContext()
	{
		return (this.expressionEvaluationContext != null);
	}

	public ExpressionEvaluationContext getExpressionEvaluationContext()
	{
		return expressionEvaluationContext;
	}

	public void setExpressionEvaluationContext(ExpressionEvaluationContext expressionEvaluationContext)
	{
		this.expressionEvaluationContext = expressionEvaluationContext;
	}

	@Override
	public SqlParamValue map(Connection cn, Table table, Column column, Object value)
			throws SqlParamValueMapperException
	{
		if (value == null)
			return null;

		if (value instanceof String)
			value = evalExpressionIf(cn, table, column, (String) value);

		SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value);

		return sqlParamValue;
	}

	/**
	 * 将源值映射为{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param value
	 * @param sqlType
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	protected SqlParamValue mapToSqlParamValue(Connection cn, Table table, Column column, Object value)
			throws SqlParamValueMapperException
	{
		if (value == null)
			return new SqlParamValue(null, column.getType());

		int sqlType = column.getType();

		SqlParamValue sqlParamValue = null;
		Object paramValue = null;

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			{
				if (value instanceof String)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, String.class);

				break;
			}

			case Types.LONGVARCHAR:
			{
				if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePath(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getReader(table, column, (File) value);
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Reader.class);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, BigDecimal.class);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				if (value instanceof Boolean)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Boolean.class);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Integer.class);

				break;
			}

			case Types.BIGINT:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Long.class);

				break;
			}

			case Types.REAL:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Float.class);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Double.class);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				if (value instanceof byte[])
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getInputStream(table, column, (File) value);
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getInputStreamIfFilePath(table, column, v);

					if (paramValue == null)
						sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, InputStream.class);
				}
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, InputStream.class);

				break;
			}

			case Types.DATE:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, java.sql.Date.class);

				break;
			}

			case Types.TIME:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, java.sql.Time.class);

				break;
			}

			case Types.TIMESTAMP:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, java.sql.Timestamp.class);

				break;
			}

			case Types.CLOB:
			{
				if (value instanceof Clob)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePath(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getReader(table, column, (File) value);
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Clob.class);

				break;
			}

			case Types.BLOB:
			{
				if (value instanceof Blob)
					paramValue = value;
				else if (value instanceof byte[])
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getInputStream(table, column, (File) value);
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getInputStreamIfFilePath(table, column, v);

					if (paramValue == null)
						sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Blob.class);
				}
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Blob.class);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			{
				if (value instanceof String)
					paramValue = value;
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, String.class);

				break;
			}

			case Types.LONGNVARCHAR:
			{
				if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePath(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getReader(table, column, (File) value);
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, Reader.class);

				break;
			}

			case Types.NCLOB:
			{
				if (value instanceof NClob)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePath(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getReader(table, column, (File) value);
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, NClob.class);

				break;
			}

			case Types.SQLXML:
			{
				if (value instanceof SQLXML)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePath(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = this.filePathValueResolver.getReader(table, column, (File) value);
				else
					sqlParamValue = convertToSqlParamValueExtWrap(cn, table, column, value, SQLXML.class);

				break;
			}

			default:

				throw new UnsupportedSqlParamValueMapperException(table, column, value);
		}

		if (sqlParamValue != null)
			return sqlParamValue;
		else
			return createSqlParamValue(column, paramValue);
	}

	protected SqlParamValue convertToSqlParamValueExtWrap(Connection cn, Table table, Column column, Object value,
			Class<?> suggestType) throws SqlParamValueMapperException
	{
		try
		{
			return convertToSqlParamValueExt(cn, table, column, value, suggestType);
		}
		catch (SqlParamValueMapperException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new SqlParamValueMapperException(t);
		}
	}

	/**
	 * 将源值转换为{@linkplain SqlParamValue}。
	 * <p>
	 * 此方法默认直接抛出{@linkplain UnsupportedSqlParamValueMapperException}，子类可以重写进行扩展。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @param suggestType
	 *            建议类型
	 * @return
	 * @throws Throwable
	 * @throws SqlParamValueMapperException
	 */
	protected SqlParamValue convertToSqlParamValueExt(Connection cn, Table table, Column column, Object value,
			Class<?> suggestType) throws Throwable, SqlParamValueMapperException
	{
		throw new UnsupportedSqlParamValueMapperException(table, column, value);
	}

	/**
	 * 如果是文件路径，则返回其输入流；否则，返回{@code null}。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 */
	protected Reader getReaderIfFilePath(Table table, Column column, String value)
	{
		File file = this.filePathValueResolver.getFileValue(table, column, value);
		return (file == null ? null : this.filePathValueResolver.getReader(table, column, file));
	}

	/**
	 * 如果是文件路径，则返回其输入流；否则，返回{@code null}。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 */
	protected InputStream getInputStreamIfFilePath(Table table, Column column, String value)
	{
		File file = this.filePathValueResolver.getFileValue(table, column, value);
		return (file == null ? null : this.filePathValueResolver.getInputStream(table, column, file));
	}

	/**
	 * 如果{@linkplain #hasExpressionEvaluationContext()}则计算表达式。
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 */
	protected String evalExpressionIf(Connection cn, Table table, Column column, String value)
	{
		if (!hasExpressionEvaluationContext())
			return value;

		List<NameExpression> variableExpressions = this.variableExpressionResolver.resolveNameExpressions(value);

		String evaluatedPropValue = value;

		if (variableExpressions != null && !variableExpressions.isEmpty())
			evaluatedPropValue = evaluateVariableExpressions(value, variableExpressions,
					this.expressionEvaluationContext);

		List<NameExpression> sqlExpressions = this.sqlExpressionResolver.resolveNameExpressions(evaluatedPropValue);

		if (sqlExpressions != null && !sqlExpressions.isEmpty())
			evaluatedPropValue = evaluateSqlExpressions(cn, evaluatedPropValue, sqlExpressions,
					this.expressionEvaluationContext);

		// 如果没有执行计算，则需要处理可能的转义表达式
		if (evaluatedPropValue == value)
		{
			evaluatedPropValue = this.variableExpressionResolver.unescape(evaluatedPropValue);
			evaluatedPropValue = this.sqlExpressionResolver.unescape(evaluatedPropValue);
		}

		return evaluatedPropValue;
	}

	/**
	 * 计算给定变量表达式的值。
	 * 
	 * @param source
	 *            变量表达式字符串
	 * @param expressions
	 * @param expressionEvaluationContext
	 * @return
	 * @throws VariableExpressionErrorException
	 */
	protected String evaluateVariableExpressions(String source, List<NameExpression> expressions,
			ExpressionEvaluationContext expressionEvaluationContext) throws VariableExpressionErrorException
	{
		List<Object> expressionValues = new ArrayList<>();

		for (int i = 0, len = expressions.size(); i < len; i++)
		{
			NameExpression expression = expressions.get(i);

			if (expressionEvaluationContext.containsCachedValue(expression))
			{
				Object value = expressionEvaluationContext.getCachedValue(expression);
				expressionValues.add(value);
			}
			else
			{
				evaluateVariableExpression(expression, expressionEvaluationContext, expressionValues);
			}
		}

		String evaluated = this.variableExpressionResolver.evaluate(source, expressions, expressionValues, "");

		return evaluated;
	}

	/**
	 * 计算变量表达式值。
	 * 
	 * @param expression
	 * @param expressionEvaluationContext
	 * @param expressionValues
	 * @throws VariableExpressionErrorException
	 */
	protected void evaluateVariableExpression(NameExpression expression,
			ExpressionEvaluationContext expressionEvaluationContext, List<Object> expressionValues)
			throws VariableExpressionErrorException
	{
		org.springframework.expression.Expression spelExpression = null;

		try
		{
			spelExpression = this.spelExpressionParser.parseExpression(expression.getContent());
		}
		catch (Exception e)
		{
			throw new VariableExpressionSyntaxErrorException(expression, e);
		}

		try
		{
			Object value = spelExpression.getValue(expressionEvaluationContext.getVariableExpressionBean());

			expressionValues.add(value);
			expressionEvaluationContext.putCachedValue(expression, value);
		}
		catch (Exception e)
		{
			throw new VariableExpressionErrorException(expression, e);
		}
	}

	/**
	 * 计算给定SQL表达式的值。
	 * 
	 * @param cn
	 * @param source
	 *            SQL表达式字符串
	 * @param expressions
	 * @param expressionEvaluationContext
	 * @return
	 * @throws SqlExpressionErrorException
	 * @throws VariableExpressionErrorException
	 */
	protected String evaluateSqlExpressions(Connection cn, String source, List<NameExpression> expressions,
			ExpressionEvaluationContext expressionEvaluationContext)
			throws SqlExpressionErrorException, VariableExpressionErrorException
	{
		List<Object> expressionValues = new ArrayList<>();

		for (int i = 0, len = expressions.size(); i < len; i++)
		{
			NameExpression expression = expressions.get(i);

			if (expressionEvaluationContext.containsCachedValue(expression))
			{
				Object value = expressionEvaluationContext.getCachedValue(expression);
				expressionValues.add(value);
			}
			else
			{
				evaluateSqlExpression(cn, expression, expressionEvaluationContext, expressionValues);
			}
		}

		String evaluated = this.sqlExpressionResolver.evaluate(source, expressions, expressionValues, "");

		return evaluated;
	}

	/**
	 * 作为SQL表达式求值。
	 * 
	 * @param cn
	 * @param dialect
	 * @param expression
	 * @param expressionEvaluationContext
	 * @param expressionValues
	 * @throws SqlExpressionErrorException
	 */
	protected void evaluateSqlExpression(Connection cn, NameExpression expression,
			ExpressionEvaluationContext expressionEvaluationContext, List<Object> expressionValues)
			throws SqlExpressionErrorException
	{
		Statement st = null;
		ResultSet rs = null;
		try
		{
			st = createQueryStatement(cn, ResultSet.TYPE_FORWARD_ONLY);
			rs = st.executeQuery(expression.getContent());

			Object value = null;

			if (rs.next())
				value = rs.getObject(1);

			expressionValues.add(value);
			expressionEvaluationContext.putCachedValue(expression, value);
		}
		catch (SQLNonTransientException e)
		{
			throw new SqlExpressionSyntaxErrorException(expression, e);
		}
		catch (SQLException e)
		{
			throw new SqlExpressionErrorException(expression, e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	protected boolean isVariableExpression(Object obj)
	{
		return this.variableExpressionResolver.isExpression(obj);
	}

	protected boolean isSqlExpression(Object obj)
	{
		return this.sqlExpressionResolver.isExpression(obj);
	}
}
