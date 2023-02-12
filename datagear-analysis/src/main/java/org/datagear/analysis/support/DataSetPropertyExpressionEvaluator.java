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
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetProperty;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * {@linkplain DataSetProperty#getExpression()}表达式计算器。
 * <p>
 * 此类用于计算{@linkplain DataSet#getProperties()}的{@linkplain DataSetProperty#getExpression()}表达式的值，支持诸如：
 * </p>
 * <p>
 * <code>"(属性名A + 属性名B)*2 - 属性名C/3"</code>
 * </p>
 * <p>
 * 的表达式计算。
 * </p>
 * <p>表达式规范：</p>
 * <p>
 * <ol>
 * <li>属性值：<br>
 * {@code 属性名}
 * </li>
 * <li>数值计算：<br>
 * 加：{@code A + B}<br>
 * 减：{@code A - B}<br>
 * 乘：{@code A * B}<br>
 * 除：{@code A / B}<br>
 * 求余：{@code A % B}<br>
 * 混合：{@code (A + B)*C - D/E + 2}
 * </li>
 * <li>比较计算：<br>
 * 小于：{@code A < B}<br>
 * 大于：{@code A > B}<br>
 * 小于等于：{@code A <= B}<br>
 * 大于等于：{@code A >= B}<br>
 * 等于：{@code A == B}<br>
 * 不等于：{@code A != B}<br>
 * </li>
 * <li>逻辑计算：<br>
 * 与：{@code A && B}<br>
 * 或：{@code A || B}<br>
 * 非：{@code !A}
 * </li>
 * <li>三元计算：<br>
 * {@code A ? B : C}
 * </li>
 * <li>函数调用：<br>
 * {@code 函数名(A, ...)}
 * </li>
 * </ol>
 * </p>
 * <p>
 * 其中，{@code A}、{@code B}、{@code C}、{@code D}、{@code E}为上述上下文合法的表达式。
 * </p>
 * <p>
 * 表达式示例：
 * </p>
 * <p>
 * {@code "属性名A >= 属性名B ? (属性名C + 属性名D)/2 : (属性名E + 属性名F)*2"}
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
	public static final DataSetPropertyExpressionEvaluator DEFAULT = new DataSetPropertyExpressionEvaluator();

	private ExpressionParser expressionParser = new SpelExpressionParser();
	
	private ConversionService conversionService = null;

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

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	/**
	 * 计算表达式的值。
	 * 
	 * @param expression
	 * @param data
	 * @return
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public Object eval(String expression, Object data) throws DataSetPropertyExpressionEvaluatorException
	{
		Expression exp = parseExpression(expression);

		try
		{
			EvaluationContext context = buildSimpleMapAccessEvaluationContext();
			return doEvalSingle(exp, context, data);
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
	 * 计算表达式的值。
	 * 
	 * @param property
	 *            {@linkplain DataSetProperty#getExpression()}不应为空
	 * @param datas
	 * @return
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public Object[] eval(String expression, Object[] datas) throws DataSetPropertyExpressionEvaluatorException
	{
		Expression exp = parseExpression(expression);

		try
		{
			EvaluationContext context = buildSimpleMapAccessEvaluationContext();
			return doEvalArray(exp, context, datas);
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
	 * 计算表达式的值。
	 * 
	 * @param expression
	 * @param datas
	 * @return
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public List<Object> eval(String expression, List<?> datas) throws DataSetPropertyExpressionEvaluatorException
	{
		Expression exp = parseExpression(expression);

		try
		{
			EvaluationContext context = buildSimpleMapAccessEvaluationContext();
			return doEvalList(exp, context, datas);
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
	 * 计算和处理表达式的值。
	 * 
	 * @param dataSetProperties
	 *            它们的{@linkplain DataSetProperty#getExpression()}应不为空
	 * @param datas
	 * @param handler
	 * @throws DataSetPropertyExpressionEvaluatorException
	 */
	public <T> void eval(List<DataSetProperty> dataSetProperties, List<T> datas, EvalPostHandler<? super T> handler)
			throws DataSetPropertyExpressionEvaluatorException
	{
		List<Expression> expressions = parseExpressions(dataSetProperties);
		int plen = dataSetProperties.size();
		
		try
		{
			EvaluationContext context = buildSimpleMapAccessEvaluationContext();

			for (T data : datas)
			{
				for (int i = 0; i < plen; i++)
				{
					DataSetProperty property = dataSetProperties.get(i);
					Expression expression = expressions.get(i);

					Object value = doEvalSingle(expression, context, data);
					handler.handle(property, i, data, value);
				}
			}
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

	protected List<Expression> parseExpressions(List<DataSetProperty> dataSetProperties)
			throws DataSetPropertyExpressionEvaluatorException
	{
		List<Expression> expressions = new ArrayList<Expression>(dataSetProperties.size());
		
		for(DataSetProperty p : dataSetProperties)
		{
			expressions.add(parseExpression(p.getExpression()));
		}
		
		return expressions;
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

	protected Object doEvalSingle(Expression expression, EvaluationContext context, Object data) throws Throwable
	{
		return expression.getValue(context, data);
	}

	protected Expression parseExpression(String expression)
			throws DataSetPropertyExpressionEvaluatorParseException, DataSetPropertyExpressionEvaluatorException
	{
		try
		{
			return this.expressionParser.parseExpression(expression);
		}
		catch (ParseException e)
		{
			throw new DataSetPropertyExpressionEvaluatorParseException(e);
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpressionEvaluatorException(t);
		}
	}

	/**
	 * 构建支持{@linkplain Map}宽松访问语法的的计算上下文。
	 * <p>
	 * Spring表达式默认对于{@linkplain Map}的访问语法为：{@code map['key']}，而数据集的结果数据几乎都是{@code Map}类型的，
	 * 这样会导致定义表达式会较繁琐且不够直观。
	 * </p>
	 * <p>
	 * 因而，此方法返回的{@linkplain EvaluationContext}做了特殊处理，支持以{@code map.key}的宽松语法访问{@linkplain Map}，
	 * 从而简化表达式定义。
	 * </p>
	 * 
	 * @return
	 */
	protected EvaluationContext buildSimpleMapAccessEvaluationContext()
	{
		//注意：这里Builder构造方法参数的MapAccessor必须在DataBindingPropertyAccessor之前，
		//才能使得"map.size"表达式优先访问"size"关键字的值而非map的大小
		SimpleEvaluationContext.Builder builder = new SimpleEvaluationContext.Builder(new MapAccessor(),
				DataBindingPropertyAccessor.forReadOnlyAccess());
		
		if(this.conversionService != null)
			builder.withConversionService(this.conversionService);
		
		return builder.build();
	}

	/**
	 * 计算后置处理器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface EvalPostHandler<T>
	{
		/**
		 * 计算完成。
		 * 
		 * @param property
		 * @params propertyIndex
		 * @param data
		 * @param value
		 *            计算结果值
		 */
		void handle(DataSetProperty property, int propertyIndex, T data, Object value);
	}
	
	/**
	 * 适用于{@linkplain DataSetProperty#getExpression()}表达式计算规范的{@linkplain Map}访问器。
	 * <p>
	 * {@linkplain Map}访问规范包括：
	 * </p>
	 * <p>
	 * 1. 只允许访问{@linkplain Map}的关键字值，不允许访问{@linkplain Map}对象本身的属性（比如{@code size}属性）。
	 * </p>
	 * <p>
	 * 2. 只允许读操作，不允许写操作。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class MapAccessor implements PropertyAccessor
	{
		public MapAccessor()
		{
			super();
		}

		@Override
		public Class<?>[] getSpecificTargetClasses()
		{
			return new Class<?>[] {Map.class};
		}

		@Override
		public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException
		{
			return (target instanceof Map);
		}

		@Override
		public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException
		{
			Object value = ((Map<?, ?>) target).get(name);
			return new TypedValue(value);
		}

		@Override
		public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException
		{
			return false;
		}

		@Override
		public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException
		{
			throw new UnsupportedOperationException();
		}
	}
}
