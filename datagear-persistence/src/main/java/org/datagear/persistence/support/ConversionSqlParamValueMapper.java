/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.persistence.support;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.LiteralSqlParamValue;
import org.datagear.persistence.SqlParamValueMapper;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.persistence.support.expression.NameExpression;
import org.datagear.persistence.support.expression.SqlExpressionResolver;
import org.datagear.persistence.support.expression.VariableExpressionResolver;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlParamValue;
import org.datagear.util.StringUtil;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 支持类型转换的{@linkplain SqlParamValueMapper}。
 * <p>
 * 对于LOB、二进制类型，此类支持文件路径（{@code "file:..."}）、Hex编码（{@code "hex:..."}）、Base64编码（{@code "base64:..."}）格式的字符串原值，<br>
 * 如果设置了{@linkplain #setFilePathValueDirectory(File)}，则文件路径限制为其子路径。
 * </p>
 * <p>
 * 对于基本类型转换，应该设置{@linkplain #setConversionService(ConversionService)}。
 * </p>
 * <p>
 * 如果设置了{@linkplain #setEnableSqlExpression(boolean)}（默认为{@code true}），此类可支持SQL表达式<code>"${...}"</code>的字符串原值。
 * </p>
 * <p>
 * 如果设置了{@linkplain #setEnableVariableExpression(boolean)}（默认为{@code true}），此类可支持变量表达式<code>"#{...}"</code>的字符串原值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConversionSqlParamValueMapper extends AbstractSqlParamValueMapper
{
	/** 文件路径值前缀 */
	public static final String PREFIX_FILE_PATH = "file:";

	/** Hex值前缀 */
	public static final String PREFIX_HEX = "hex:";

	/** Base64值前缀 */
	public static final String PREFIX_BASE64 = "base64:";

	protected static final VariableExpressionResolver DEFAULT_VARIABLE_EXPRESSION_RESOLVER = new VariableExpressionResolver();
	protected static final SqlExpressionResolver DEFAULT_SQL_EXPRESSION_RESOLVER = new SqlExpressionResolver();
	protected static final SpelExpressionParser DEFAULT_SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

	/** 用于支持基本类型转换的转换服务类 */
	private ConversionService conversionService = null;

	/** 文件路径值所在的目录 */
	private File filePathValueDirectory = null;

	/** 是否开启SQL表达式特性 */
	private boolean enableSqlExpression = true;

	/** 是否开启变量表达式特性 */
	private boolean enableVariableExpression = true;

	/** 变量表达式解析器 */
	private VariableExpressionResolver variableExpressionResolver = DEFAULT_VARIABLE_EXPRESSION_RESOLVER;

	/** SQL表达式解析器 */
	private SqlExpressionResolver sqlExpressionResolver = DEFAULT_SQL_EXPRESSION_RESOLVER;

	/** 变量表达式计算器 */
	private SpelExpressionParser spelExpressionParser = DEFAULT_SPEL_EXPRESSION_PARSER;

	/** 表达式计算上下文 */
	private ExpressionEvaluationContext expressionEvaluationContext = new ExpressionEvaluationContext();

	public ConversionSqlParamValueMapper()
	{
		super();
	}

	public boolean hasConversionService()
	{
		return (this.conversionService != null);
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	public boolean hasFilePathValueDirectory()
	{
		return (this.filePathValueDirectory != null);
	}

	public File getFilePathValueDirectory()
	{
		return filePathValueDirectory;
	}

	public void setFilePathValueDirectory(File filePathValueDirectory)
	{
		this.filePathValueDirectory = filePathValueDirectory;
	}

	public boolean isEnableSqlExpression()
	{
		return enableSqlExpression;
	}

	public void setEnableSqlExpression(boolean enableSqlExpression)
	{
		this.enableSqlExpression = enableSqlExpression;
	}

	public boolean isEnableVariableExpression()
	{
		return enableVariableExpression;
	}

	public void setEnableVariableExpression(boolean enableVariableExpression)
	{
		this.enableVariableExpression = enableVariableExpression;
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
			return createSqlParamValue(column, null);

		try
		{
			value = resolveExpressionIf(cn, table, column, value);

			if (value instanceof SqlParamValue)
				return (SqlParamValue) value;

			SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value);
			return sqlParamValue;
		}
		catch (SqlParamValueMapperException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new SqlParamValueMapperException(table, column, value, t);
		}
	}

	/**
	 * 处理可能的表达式。
	 * <p>
	 * 可能返回{@linkplain LiteralSqlParamValue}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected Object resolveExpressionIf(Connection cn, Table table, Column column, Object value) throws Throwable
	{
		if (!(value instanceof String))
			return value;

		String valueStr = (String) value;
		Object result = valueStr;

		if (this.enableVariableExpression)
		{
			List<NameExpression> expressions = this.variableExpressionResolver.resolveNameExpressions(valueStr);

			if (!expressions.isEmpty())
				result = evaluateVariableExpressions(cn, table, column, valueStr, expressions,
						this.expressionEvaluationContext);
			else
				result = this.variableExpressionResolver.unescape(valueStr);
		}

		if (this.enableSqlExpression && (result instanceof String))
		{
			valueStr = (String) result;

			List<NameExpression> expressions = this.sqlExpressionResolver.resolveNameExpressions(valueStr);

			if (!expressions.isEmpty())
				result = evaluateSqlExpressions(cn, table, column, valueStr, expressions,
						this.expressionEvaluationContext);
			else
				result = this.sqlExpressionResolver.unescape(valueStr);
		}

		return result;
	}

	/**
	 * 计算变量表达式列值。
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @param expressions
	 * @param expressionEvaluationContext
	 * @return
	 * @throws Throwable
	 */
	protected String evaluateVariableExpressions(Connection cn, Table table, Column column, String value,
			List<NameExpression> expressions, ExpressionEvaluationContext expressionEvaluationContext) throws Throwable
	{
		List<Object> expressionValues = new ArrayList<>(expressions.size());

		for (int i = 0, len = expressions.size(); i < len; i++)
		{
			NameExpression expression = expressions.get(i);

			String expressionKey = expressionEvaluationContext.getCachedKey(expression);
			if (expressionEvaluationContext.containsCachedValue(expressionKey))
			{
				Object expValue = expressionEvaluationContext.getCachedValue(expressionKey);
				expressionValues.add(expValue);
			}
			else
			{
				evaluateVariableExpression(cn, table, column, value, expression, expressionEvaluationContext,
						expressionValues);
			}
		}

		String evaluated = this.variableExpressionResolver.evaluate(value, expressions, expressionValues, "");

		return evaluated;
	}

	protected Object evaluateVariableExpression(Connection cn, Table table, Column column, String value,
			NameExpression expression, ExpressionEvaluationContext expressionEvaluationContext,
			List<Object> expressionValues) throws Throwable
	{
		Object expValue;

		org.springframework.expression.Expression spelExpression = null;

		try
		{
			spelExpression = this.spelExpressionParser.parseExpression(expression.getContent());
		}
		catch (Throwable t)
		{
			// 如果是表达式不合法，且列是文本类型，则忽略计算
			if (JdbcUtil.isTextType(column.getType()))
				expValue = expression.toString();
			else
				throw new SqlParamValueVariableExpressionSyntaxException(table, column, value, expression.getContent(),
						t);
		}

		try
		{
			expValue = spelExpression.getValue(expressionEvaluationContext.getVariableExpressionBean());
		}
		catch (Throwable t)
		{
			// 如果是表达式不合法，且列是文本类型，则忽略计算
			if (JdbcUtil.isTextType(column.getType()))
				expValue = expression.toString();
			else
				throw new SqlParamValueVariableExpressionException(table, column, value, expression.getContent(), t);
		}

		expressionValues.add(expValue);
		expressionEvaluationContext.putCachedValue(expression, expValue);

		return expValue;
	}

	/**
	 * 计算SQL表达式的值。
	 * <p>
	 * 可能返回{@linkplain LiteralSqlParamValue}。
	 * </p>
	 */
	protected Object evaluateSqlExpressions(Connection cn, Table table, Column column, String value,
			List<NameExpression> expressions, ExpressionEvaluationContext expressionEvaluationContext) throws Throwable
	{
		// 如果value是严格SQL表达式，那么直接返回LiteralSqlParamValue
		if (expressions.size() == 1)
		{
			NameExpression expression = expressions.get(0);
			if (this.sqlExpressionResolver.isExpressionStrict(value, expression))
			{
				String sql;

				String expressionKey = expressionEvaluationContext.getCachedKey(expression);
				if (expressionEvaluationContext.containsCachedValue(expressionKey))
					sql = (String) expressionEvaluationContext.getCachedValue(expressionKey);
				else
				{
					sql = expression.getContent();
					expressionEvaluationContext.putCachedValue(expression, sql);
				}

				return new LiteralSqlParamValue(sql, column.getType());
			}
		}

		List<Object> expressionValues = new ArrayList<>(expressions.size());

		for (int i = 0, len = expressions.size(); i < len; i++)
		{
			NameExpression expression = expressions.get(i);
			String sql;

			String expressionKey = expressionEvaluationContext.getCachedKey(expression);
			if (expressionEvaluationContext.containsCachedValue(expressionKey))
				sql = (String) expressionEvaluationContext.getCachedValue(expressionKey);
			else
			{
				sql = expression.getContent();
				expressionEvaluationContext.putCachedValue(expression, sql);
			}

			Object sqlValue = evaluateSqlExpressionResultIfSelect(cn, table, column, value, expression, sql);
			expressionValues.add(sqlValue);
		}

		String evaluated = this.sqlExpressionResolver.evaluate(value, expressions, expressionValues, "");

		return evaluated;
	}

	protected Object evaluateSqlExpressionResultIfSelect(Connection cn, Table table, Column column, String value,
			NameExpression expression, String sql) throws Throwable
	{
		if (!DefaultPersistenceManager.isSelectSql(sql))
			return expression.toString();

		Statement st = null;
		ResultSet rs = null;
		try
		{
			st = createQueryStatement(cn, ResultSet.TYPE_FORWARD_ONLY);
			rs = st.executeQuery(sql);

			Object sqlResult = null;

			if (rs.next())
				sqlResult = rs.getObject(1);

			return sqlResult;
		}
		catch (SQLNonTransientException e)
		{
			if (JdbcUtil.isTextType(column.getType()))
				return sql;
			else
				throw new SqlParamValueSqlExpressionSyntaxException(table, column, value, sql, e);
		}
		catch (SQLException e)
		{
			if (JdbcUtil.isTextType(column.getType()))
				return sql;
			else
				throw new SqlParamValueSqlExpressionException(table, column, value, sql, e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 将值映射为{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param value
	 * @param sqlType
	 * @return
	 * @throws Throwable
	 */
	protected SqlParamValue mapToSqlParamValue(Connection cn, Table table, Column column, Object value) throws Throwable
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
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, String.class);

				break;
			}

			case Types.LONGVARCHAR:
			{
				if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePathExists(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof File)
					paramValue = IOUtil.getInputStream((File) value);
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Reader.class);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, BigDecimal.class);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				if (value instanceof Boolean)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Boolean.class);

				break;
			}

			case Types.TINYINT:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Byte.class, Short.class, Integer.class);

				break;
			}
			
			case Types.SMALLINT:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Short.class, Integer.class, Long.class);

				break;
			}
			
			case Types.INTEGER:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Integer.class, Long.class, BigInteger.class);

				break;
			}

			case Types.BIGINT:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Long.class, BigInteger.class);

				break;
			}

			case Types.REAL:
			case Types.FLOAT:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Float.class, Double.class, BigDecimal.class);

				break;
			}
			
			case Types.DOUBLE:
			{
				if (value instanceof Number)
					paramValue = value;
				else
					//实际的数值可能超出JDBC规范推荐的数据类型，因此需要尝试更大的类型
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Double.class, BigDecimal.class);

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
					paramValue = IOUtil.getInputStream((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (StringUtil.isEmpty(v))
						paramValue = null;
					else
					{
						paramValue = getInputStreamIfFilePath(table, column, v);

						if (paramValue == null)
							paramValue = getIfBytesValue(table, column, v);

						if (paramValue == null)
							sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, InputStream.class);
					}
				}
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, InputStream.class);

				break;
			}

			case Types.DATE:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, java.sql.Date.class);

				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, java.sql.Time.class);

				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				if (value instanceof Date)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, java.sql.Timestamp.class);

				break;
			}

			case Types.CLOB:
			{
				if (value instanceof Clob)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePathExists(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof File)
					paramValue = IOUtil.getInputStream((File) value);
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Clob.class);

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
					paramValue = IOUtil.getInputStream((File) value);
				else if (value instanceof String)
				{
					String v = (String) value;

					if (StringUtil.isEmpty(v))
						paramValue = null;
					else
					{
						paramValue = getInputStreamIfFilePath(table, column, v);

						if (paramValue == null)
							paramValue = getIfBytesValue(table, column, v);

						if (paramValue == null)
							sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Blob.class);
					}
				}
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Blob.class);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			{
				if (value instanceof String)
					paramValue = value;
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, String.class);

				break;
			}

			case Types.LONGNVARCHAR:
			{
				if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePathExists(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof File)
					paramValue = IOUtil.getInputStream((File) value);
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, Reader.class);

				break;
			}

			case Types.NCLOB:
			{
				if (value instanceof NClob)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePathExists(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = IOUtil.getInputStream((File) value);
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, NClob.class);

				break;
			}

			case Types.SQLXML:
			{
				if (value instanceof SQLXML)
					paramValue = value;
				else if (value instanceof String)
				{
					String v = (String) value;
					paramValue = getReaderIfFilePathExists(table, column, v);

					if (paramValue == null)
						paramValue = v;
				}
				else if (value instanceof Reader)
					paramValue = value;
				else if (value instanceof InputStream)
					paramValue = value;
				else if (value instanceof File)
					paramValue = IOUtil.getInputStream((File) value);
				else
					sqlParamValue = mapToSqlParamValueExt(cn, table, column, value, SQLXML.class);

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

	/**
	 * 将源值映射为{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @param suggestTypes
	 *            建议类型
	 * @return
	 * @throws Throwable
	 */
	protected SqlParamValue mapToSqlParamValueExt(Connection cn, Table table, Column column, Object value,
			Class<?>... suggestTypes) throws Throwable
	{
		if (this.conversionService != null)
		{
			ConversionFailedException e0 = null;
			
			for(int i=0; i<suggestTypes.length; i++)
			{
				try
				{
					Object target = this.conversionService.convert(value, suggestTypes[i]);
					return createSqlParamValue(column, target);
				}
				catch(ConversionFailedException e)
				{
					if(i == 0)
						e0 = e;
				}
			}
			
			throw e0;
		}
		else
			throw new UnsupportedSqlParamValueMapperException(table, column, value);
	}

	/**
	 * 如果是文件路径，且所表示的文件存在，则返回其输入流；否则，返回{@code null}。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected Reader getReaderIfFilePathExists(Table table, Column column, String value) throws Throwable
	{
		if (!isFilePathValue(value))
			return null;

		File file = getFileObject(table, column, value);

		// 因为映射目标允许为字符串，所以如果文件不存在，则当其是普通字符串
		if (!file.exists())
			return null;

		String charset = getFileCharset(table, column, value);

		// XML默认为UTF-8
		if (Types.SQLXML == column.getType() && StringUtil.isEmpty(charset))
			charset = "UTF-8";

		return IOUtil.getReader(file, charset);
	}

	/**
	 * 如果是文件路径，则返回其输入流；否则，返回{@code null}。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected InputStream getInputStreamIfFilePath(Table table, Column column, String value) throws Throwable
	{
		if (!isFilePathValue(value))
			return null;

		File file = getFileObject(table, column, value);

		// 映射目标不允许是字符串，所以这里抛出异常
		if (!file.exists())
			throw new SqlParamValueMapperException(table, column, value, "File [" + value + "] not found");

		return IOUtil.getInputStream(file);
	}

	/**
	 * 获取{@linkplain #isFilePathValue(String)}字符串的文件对象。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected File getFileObject(Table table, Column column, String value) throws Throwable
	{
		String filePath = getActualFilePath(value);

		if (this.filePathValueDirectory != null)
			return FileUtil.getFile(this.filePathValueDirectory, filePath);
		else
			return FileUtil.getFile(filePath);
	}

	/**
	 * 获取{@linkplain #isFilePathValue(String)}字符串的文件字符集。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return 返回{@code null}表示无字符集
	 * @throws Throwable
	 */
	protected String getFileCharset(Table table, Column column, String value) throws Throwable
	{
		return null;
	}

	/**
	 * 给定字符串是否表示文件路径。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	public boolean isFilePathValue(String value) throws Throwable
	{
		return (value != null && value.startsWith(PREFIX_FILE_PATH));
	}

	/**
	 * 获取{@linkplain #isFilePathValue(String)}字符串的文件路径。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected String getActualFilePath(String value) throws Throwable
	{
		return value.substring(PREFIX_FILE_PATH.length());
	}

	/**
	 * 如果字符串表示字节数组，则返回字节数组；否则，返回{@code null}。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return 返回{@code null}表示不是字节数组
	 * @throws Throwable
	 */
	public byte[] getIfBytesValue(Table table, Column column, String value) throws Throwable
	{
		if (!isBytesValue(value))
			return null;

		return valueToBytes(value);
	}

	/**
	 * 给定字符串是否表示字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	public boolean isBytesValue(String value) throws Throwable
	{
		if (value == null)
			return false;

		return (value.startsWith(PREFIX_HEX) || value.startsWith(PREFIX_BASE64));
	}

	/**
	 * 将{@linkplain #isBytesValue(String)}字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected byte[] valueToBytes(String value) throws Throwable
	{
		byte[] bytes = null;

		if (value.startsWith(PREFIX_HEX))
			bytes = valueToBytesForHex(value.substring(PREFIX_HEX.length()));
		else if (value.startsWith(PREFIX_BASE64))
			bytes = valueToBytesForBase64(value.substring(PREFIX_BASE64.length()));

		return bytes;
	}

	/**
	 * 将Hex编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected byte[] valueToBytesForHex(String value) throws Throwable
	{
		if (value == null || value.isEmpty())
			return null;

		if (value.startsWith("0x") || value.startsWith("0X"))
			value = value.substring(2);

		return Hex.decodeHex(value);
	}

	/**
	 * 将Base64编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws Throwable
	 */
	protected byte[] valueToBytesForBase64(String value) throws Throwable
	{
		if (value == null || value.isEmpty())
			return null;

		return Base64.getDecoder().decode(value);
	}

}
