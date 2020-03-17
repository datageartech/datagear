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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PstParamMapper;
import org.datagear.persistence.PstParamMapperException;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.persistence.support.expression.NameExpression;
import org.datagear.persistence.support.expression.SqlExpressionErrorException;
import org.datagear.persistence.support.expression.SqlExpressionResolver;
import org.datagear.persistence.support.expression.SqlExpressionSyntaxErrorException;
import org.datagear.persistence.support.expression.VariableExpressionErrorException;
import org.datagear.persistence.support.expression.VariableExpressionResolver;
import org.datagear.persistence.support.expression.VariableExpressionSyntaxErrorException;
import org.datagear.util.JdbcUtil;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 支持类型转换的{@linkplain PstParamMapper}。
 * <p>
 * 如果{@linkplain #hasExpressionEvaluationContext()}，它还支持变量表达式<code>"#{...}"</code>、
 * SQL表达式<code>"${select...}"</code>。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConversionPstParamMapper extends PersistenceSupport implements PstParamMapper
{
	protected static final VariableExpressionResolver DEFAULT_VARIABLE_EXPRESSION_RESOLVER = new VariableExpressionResolver();

	protected static final SqlExpressionResolver DEFAULT_SQL_EXPRESSION_RESOLVER = new SqlExpressionResolver();
	
	protected static final SpelExpressionParser DEFAULT_SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
	
	protected static final FilePathValueResolver DEFAULT_FILE_PATH_VALUE_RESOLVER = new FilePathValueResolver();

	private ConversionService conversionService;

	/** 变量表达式解析器 */
	private VariableExpressionResolver variableExpressionResolver = DEFAULT_VARIABLE_EXPRESSION_RESOLVER;

	/** SQL表达式解析器 */
	private SqlExpressionResolver sqlExpressionResolver = DEFAULT_SQL_EXPRESSION_RESOLVER;

	/** 变量表达式计算器 */
	private SpelExpressionParser spelExpressionParser = DEFAULT_SPEL_EXPRESSION_PARSER;
	
	private FilePathValueResolver filePathValueResolver = DEFAULT_FILE_PATH_VALUE_RESOLVER;

	/** 表达式计算上下文 */
	private ExpressionEvaluationContext expressionEvaluationContext = null;

	public ConversionPstParamMapper()
	{
		super();
	}

	public ConversionPstParamMapper(ConversionService conversionService)
	{
		super();
		this.conversionService = conversionService;
	}

	public ConversionPstParamMapper(ConversionService conversionService,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		super();
		this.conversionService = conversionService;
		this.expressionEvaluationContext = expressionEvaluationContext;
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
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
	public Object map(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstParamMapperException
	{
		if (value == null)
			return null;

		if (value instanceof String)
			value = evalExpressionIf(cn, dialect, table, column, (String) value);

		value = convertToPstParam(cn, value, column.getType());

		return value;
	}

	/**
	 * 将源值转换为可用于{@linkplain PreparedStatement}参数值的对象。
	 * 
	 * @param cn
	 * @param value
	 * @param sqlType
	 * @return
	 * @throws PstParamMapperException
	 */
	public Object convertToPstParam(Connection cn, Object value, int sqlType) throws PstParamMapperException
	{
		if (value == null)
			return null;

		Object pstParam = null;

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			{
				if (value instanceof String)
					pstParam = value;
				else
					pstParam = convertToType(value, String.class);

				break;
			}

			case Types.LONGVARCHAR:
			{
				if (value instanceof String)
				{
					String v = (String) value;
					
					if(this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getReader(v);
					else
						pstParam = v;
				}
				else if (value instanceof Reader)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getReader((File) value);
				else
					pstParam = convertToType(value, Reader.class);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				pstParam = convertToType(value, BigDecimal.class);
				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				pstParam = convertToType(value, Boolean.class);
				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				pstParam = convertToType(value, Integer.class);
				break;
			}

			case Types.BIGINT:
			{
				pstParam = convertToType(value, Long.class);
				break;
			}

			case Types.REAL:
			{
				pstParam = convertToType(value, Float.class);
				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				pstParam = convertToType(value, Double.class);
				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				if (value instanceof byte[])
					pstParam = value;
				else if (value instanceof InputStream)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getInputStream((File) value);
				else
					pstParam = convertToType(value, InputStream.class);

				break;
			}

			case Types.DATE:
			{
				pstParam = convertToType(value, java.sql.Date.class);
				break;
			}

			case Types.TIME:
			{
				pstParam = convertToType(value, java.sql.Time.class);
				break;
			}

			case Types.TIMESTAMP:
			{
				pstParam = convertToType(value, java.sql.Timestamp.class);
				break;
			}

			case Types.CLOB:
			{
				if (value instanceof Clob)
					pstParam = value;
				else if (value instanceof Reader)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getReader((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getReader(v);
					else
						pstParam = convertToType(value, Reader.class);
				}
				else
					pstParam = convertToType(value, Reader.class);

				break;
			}

			case Types.BLOB:
			{
				if (value instanceof Blob)
					pstParam = value;
				else if (value instanceof InputStream)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getInputStream((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getInputStream(v);
					else
						pstParam = convertToType(value, InputStream.class);
				}
				else
					pstParam = convertToType(value, InputStream.class);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			{
				if (value instanceof String)
					pstParam = value;
				else
					pstParam = convertToType(value, String.class);

				break;
			}

			case Types.LONGNVARCHAR:
			{
				if (value instanceof String)
					pstParam = value;
				else if (value instanceof Reader)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getReader((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getReader(v);
					else
						pstParam = convertToType(value, Reader.class);
				}
				else
					pstParam = convertToType(value, Reader.class);

				break;
			}

			case Types.NCLOB:
			{
				if (value instanceof NClob)
					pstParam = value;
				else if (value instanceof Reader)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getReader((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getReader(v);
					else
						pstParam = convertToType(value, Reader.class);
				}
				else
					pstParam = convertToType(value, Reader.class);

				break;
			}

			case Types.SQLXML:
			{
				if (value instanceof SQLXML)
					pstParam = value;
				else if (value instanceof Reader)
					pstParam = value;
				else if (value instanceof File)
					pstParam = this.filePathValueResolver.getReader((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (this.filePathValueResolver.isFilePathValue(v))
						pstParam = this.filePathValueResolver.getReader(v);
					else
						pstParam = convertToType(value, Reader.class);
				}
				else
					pstParam = convertToType(value, Reader.class);

				break;
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}

		return pstParam;
	}

	@SuppressWarnings("unchecked")
	protected Object convertToType(Object src, Class<?> type) throws PstParamMapperException
	{
		try
		{
			return this.conversionService.convert(src, (Class<Object>) type);
		}
		catch(Throwable t)
		{
			throw new PstParamMapperException(t);
		}
	}

	/**
	 * 如果{@linkplain #hasExpressionEvaluationContext()}则计算表达式。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 */
	protected String evalExpressionIf(Connection cn, Dialect dialect, Table table, Column column, String value)
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
			evaluatedPropValue = evaluateSqlExpressions(cn, dialect, evaluatedPropValue, sqlExpressions,
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
	 * @param source                      变量表达式字符串
	 * @param expressions
	 * @param expressionEvaluationContext
	 * @return
	 * @throws VariableExpressionErrorException
	 */
	protected String evaluateVariableExpressions(String source, List<NameExpression> expressions,
			ExpressionEvaluationContext expressionEvaluationContext) throws VariableExpressionErrorException
	{
		List<Object> expressionValues = new ArrayList<Object>();

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
		catch(Exception e)
		{
			throw new VariableExpressionSyntaxErrorException(expression, e);
		}

		try
		{
			Object value = spelExpression.getValue(expressionEvaluationContext.getVariableExpressionBean());

			expressionValues.add(value);
			expressionEvaluationContext.putCachedValue(expression, value);
		}
		catch(Exception e)
		{
			throw new VariableExpressionErrorException(expression, e);
		}
	}

	/**
	 * 计算给定SQL表达式的值。
	 * 
	 * @param cn
	 * @param dialect
	 * @param source                      SQL表达式字符串
	 * @param expressions
	 * @param expressionEvaluationContext
	 * @return
	 * @throws SqlExpressionErrorException
	 * @throws VariableExpressionErrorException
	 */
	protected String evaluateSqlExpressions(Connection cn, Dialect dialect, String source,
			List<NameExpression> expressions,
			ExpressionEvaluationContext expressionEvaluationContext)
			throws SqlExpressionErrorException, VariableExpressionErrorException
	{
		List<Object> expressionValues = new ArrayList<Object>();

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
				evaluateSelectSqlExpression(cn, dialect, expression, expressionEvaluationContext, expressionValues);
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
	protected void evaluateSelectSqlExpression(Connection cn,
			Dialect dialect, NameExpression expression,
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
		catch(SQLNonTransientException e)
		{
			throw new SqlExpressionSyntaxErrorException(expression, e);
		}
		catch(SQLException e)
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
