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

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * 计算{@linkplain DataSetProperty}的表达式计算器。
 * <p>
 * 此类用于计算{@linkplain DataSetProperty#getExpression()}表达式的值，支持诸如：
 * </p>
 * <p>
 * <code>(属性名A + 属性名B)*2 - 属性名C/3</code>
 * </p>
 * <p>
 * 的表达式求值计算。
 * </p>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetPropertyExpressionEvaluator
{
	private ExpressionParser expressionParser = new SpelExpressionParser();

	public DataSetPropertyExpressionEvaluator()
	{
		super();
	}

	public ExpressionParser getExpressionParser()
	{
		return expressionParser;
	}

	public void setExpressionParser(ExpressionParser expressionParser)
	{
		this.expressionParser = expressionParser;
	}

	/**
	 * 对计算{@linkplain DataSetProperty}进行表达式计算。
	 * 
	 * @param property
	 *            {@linkplain DataSetProperty#getExpression()}不应为空
	 * @param data
	 * @return
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public Object eval(DataSetProperty property, Object data) throws DataSetPropertyExpressionEvaluatorException
	{
		String expression = property.getExpression();

		try
		{
			return doEvalSingle(expression, data);
		}
		catch (DataSetPropertyExpressionEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpressionEvaluatorException(t);
		}
	}

	/**
	 * 对计算{@linkplain DataSetProperty}进行表达式计算。
	 * 
	 * @param property
	 *            {@linkplain DataSetProperty#getExpression()}不应为空
	 * @param datas
	 * @return 当{@linkplain DataSetProperty#getExpression()}为空时将返回{@code null}
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public Object[] eval(DataSetProperty property, Object[] datas) throws DataSetPropertyExpressionEvaluatorException
	{
		String expression = property.getExpression();

		try
		{
			return doEvalArray(expression, datas);
		}
		catch (DataSetPropertyExpressionEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpressionEvaluatorException(t);
		}
	}

	/**
	 * 对计算{@linkplain DataSetProperty}进行表达式计算。
	 * 
	 * @param property
	 *            {@linkplain DataSetProperty#getExpression()}不应为空
	 * @param datas
	 * @return 当{@linkplain DataSetProperty#getExpression()}为空时将返回{@code null}
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public List<Object> eval(DataSetProperty property, List<?> datas)
			throws DataSetPropertyExpressionEvaluatorException
	{
		String expression = property.getExpression();

		try
		{
			return doEvalList(expression, datas);
		}
		catch (DataSetPropertyExpressionEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpressionEvaluatorException(t);
		}
	}

	protected Object[] doEvalArray(String expression, Object[] datas) throws Throwable
	{
		Expression exp = this.expressionParser.parseExpression(expression);
		EvaluationContext context = buildMapFirstEvaluationContext();

		return doEvalArray(exp, context, datas);
	}

	protected Object[] doEvalArray(Expression expression, EvaluationContext context, Object[] datas) throws Throwable
	{
		Object[] re = new Object[datas.length];

		for (int i = 0; i < datas.length; i++)
		{
			re[i] = doEvalSingle(expression, context, datas[i]);
		}

		return re;
	}

	protected List<Object> doEvalList(String expression, List<?> datas) throws Throwable
	{
		Expression exp = this.expressionParser.parseExpression(expression);
		EvaluationContext context = buildMapFirstEvaluationContext();

		return doEvalList(exp, context, datas);
	}

	protected List<Object> doEvalList(Expression expression, EvaluationContext context, List<?> datas)
			throws Throwable
	{
		List<Object> re = new ArrayList<Object>(datas.size());

		for (int i = 0, len = datas.size(); i < len; i++)
		{
			re.add(doEvalSingle(expression, context, datas.get(i)));
		}

		return re;
	}

	protected Object doEvalSingle(String expression, Object data) throws Throwable
	{
		Expression exp = this.expressionParser.parseExpression(expression);
		EvaluationContext context = buildMapFirstEvaluationContext();

		return doEvalSingle(exp, context, data);
	}

	protected Object doEvalSingle(Expression expression, EvaluationContext context, Object data) throws Throwable
	{
		return expression.getValue(context, data);
	}

	/**
	 * 构建{@code Map}优先的计算上下文。
	 * <p>
	 * Spring默认对于{@code Map}的访问格式为：{@code map['key']}，而数据集的结果数据几乎都是{@code Map}类型的，
	 * 这导致定义表达式会较繁琐且不够直观。
	 * </p>
	 * <p>
	 * 因而，此方法返回的{@linkplain EvaluationContext}做了特殊处理，支持以{@code map.key}的格式读取{@code Map}，
	 * 从而简化表达式定义。
	 * </p>
	 * 
	 * @return
	 */
	protected EvaluationContext buildMapFirstEvaluationContext()
	{
		SimpleEvaluationContext.Builder builder = new SimpleEvaluationContext.Builder(new MapAccessor(),
				DataBindingPropertyAccessor.forReadOnlyAccess());
		EvaluationContext context = builder.build();

		return context;
	}
}
