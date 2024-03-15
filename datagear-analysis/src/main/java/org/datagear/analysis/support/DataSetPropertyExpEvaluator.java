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

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetProperty;
import org.datagear.util.StringUtil;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.expression.spel.ast.BeanReference;
import org.springframework.expression.spel.ast.ConstructorReference;
import org.springframework.expression.spel.ast.InlineList;
import org.springframework.expression.spel.ast.InlineMap;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.expression.spel.ast.QualifiedIdentifier;
import org.springframework.expression.spel.ast.Selection;
import org.springframework.expression.spel.ast.TypeReference;
import org.springframework.expression.spel.standard.SpelExpression;
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
 * <p>
 * 表达式语法规范：
 * </p>
 * <p>
 * <ol>
 * <li>属性值：<br>
 * {@linkplain Map}简化语法：{@code 属性名}、{@code 属性名.属性名}<br>
 * {@linkplain Map}标准语法：{@code ['属性名']}、{@code ["属性名"]}、{@code 属性名['属性名']}、{@code 属性名["属性名"]}<br>
 * {@code JavaBean}：{@code 属性名}、{@code 属性名.属性名}<br>
 * {@linkplain List}、数组：{@code [索引数值]}、{@code 属性名[索引数值]}<br>
 * </li>
 * <li>数值计算：<br>
 * 加：{@code A + B}<br>
 * 减：{@code A - B}<br>
 * 乘：{@code A * B}<br>
 * 除：{@code A / B}<br>
 * 求余：{@code A % B}<br>
 * 混合：{@code (A + B)*C - D/E + 2}<br>
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
 * 非：{@code !A}<br>
 * </li>
 * <li>三元计算：<br>
 * {@code A ? B : C}<br>
 * </li>
 * <li>字符串拼接：<br>
 * {@code A + B}、{@code A + B + 'suffix'}<br>
 * </li>
 * <li>字面值：<br>
 * 数值：{@code 3}、{@code 5}、{@code 26.13}<br>
 * 字符串：{@code 'abc'}、{@code "abc"}<br>
 * 布尔值：{@code true}、{@code false}<br>
 * NULL：{@code null}<br>
 * </li>
 * </ol>
 * </p>
 * <p>
 * 其中，{@code A}、{@code B}、{@code C}、{@code D}、{@code E}为上述上下文合法的表达式。
 * </p>
 * <p>
 * 对于{@linkplain #eval(List, List, ValueSetter)}方法，
 * 表达式中的{@code 属性名}可以是{@code properties}列表中任意的{@linkplain DataSetProperty#getName()}，取值规范如下所示：
 * </p>
 * <p>
 * <ol>
 * <li>非计算属性：{@code datas}元素对象的对应属性值<br>
 * </li>
 * <li>自身属性：{@code datas}元素对象的对应原始属性值<br>
 * </li>
 * <li>前置计算属性：属性表达式计算结果值<br>
 * </li>
 * <li>后置计算属性：{@code datas}元素对象的对应原始属性值<br>
 * </li>
 * </ol>
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
public class DataSetPropertyExpEvaluator
{
	public static final DataSetPropertyExpEvaluator DEFAULT = new DataSetPropertyExpEvaluator();

	/**
	 * 表达式解析器。
	 * <p>
	 * 这里考虑安全，明确禁止了它的{@code autoGrowNullReferences}、{@code autoGrowCollections}特性
	 * </p>
	 */
	private ExpressionParser expressionParser = new SpelExpressionParser(new SpelParserConfiguration(false, false));
	
	private ConversionService conversionService = null;

	public DataSetPropertyExpEvaluator()
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
	 * @throws DataSetPropertyExpEvaluatorException
	 */
	public Object eval(String expression, Object data) throws DataSetPropertyExpEvaluatorException
	{
		try
		{
			Expression exp = parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalSingle(exp, context, data);
		}
		catch (DataSetPropertyExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpEvaluatorException(t);
		}
	}

	/**
	 * 计算表达式的值。
	 * 
	 * @param property
	 *            {@linkplain DataSetProperty#getExpression()}不应为空
	 * @param datas
	 * @return
	 * @throws DataSetPropertyExpEvaluatorException
	 */
	public Object[] eval(String expression, Object[] datas) throws DataSetPropertyExpEvaluatorException
	{
		try
		{
			Expression exp = parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalArray(exp, context, datas);
		}
		catch (DataSetPropertyExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpEvaluatorException(t);
		}
	}

	/**
	 * 计算表达式的值。
	 * 
	 * @param expression
	 * @param datas
	 * @return
	 * @throws DataSetPropertyExpEvaluatorException
	 */
	public List<Object> eval(String expression, List<?> datas) throws DataSetPropertyExpEvaluatorException
	{
		try
		{
			Expression exp = parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalList(exp, context, datas);
		}
		catch (DataSetPropertyExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpEvaluatorException(t);
		}
	}

	/**
	 * 统一计算和设置{@linkplain DataSetProperty#getExpression()}的值。
	 * <p>
	 * 这里统一处理表达式计算，使得{@linkplain DataSetProperty#getExpression()}可具有更灵活的支持规范，
	 * 具体参考此类说明：{@linkplain DataSetPropertyExpEvaluator}。
	 * </p>
	 * 
	 * @param properties
	 * @param datas
	 * @param valueSetter
	 * @returns {@code true} 执行了计算和设置；{@code false}
	 *          未执行计算和设置，因为{@code properties}中没有需计算的
	 * @throws DataSetPropertyExpEvaluatorException
	 */
	public <T> boolean eval(List<DataSetProperty> properties, List<T> datas, ValueSetter<? super T> valueSetter)
			throws DataSetPropertyExpEvaluatorException
	{
		int plen = properties.size();
		List<Expression> expressions = new ArrayList<Expression>(plen);
		int count = parseExpressions(properties, expressions);
		
		if (count < 1)
			return false;

		DataSetProperty property = null;

		try
		{
			EvaluationContext context = buildEvaluationContext();

			for (T data : datas)
			{
				// 必须按顺序计算，确保表达式中的属性取值逻辑符合规范
				for (int i = 0; i < plen; i++)
				{
					property = properties.get(i);
					Expression expression = expressions.get(i);

					if (expression != null)
					{
						Object value = doEvalSingle(expression, context, data);
						valueSetter.set(property, i, data, value);
					}
				}
			}

			return true;
		}
		catch (DataSetPropertyExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpEvaluatorException(t, (property == null ? "" : property.getName()));
		}
	}

	/**
	 * 解析表达式对象。
	 * 
	 * @param properties
	 * @param expressions
	 *            用于写入表达式的列表，如果某个元素为{@code null}，表示不是计算属性
	 * @returns 计算属性的个数
	 * @throws DataSetPropertyExpEvaluatorException
	 */
	protected int parseExpressions(List<DataSetProperty> properties, List<Expression> expressions)
			throws DataSetPropertyExpEvaluatorException
	{
		int count = 0;
		
		for(DataSetProperty p : properties)
		{
			if (isEvaluatedProperty(p))
			{
				Expression expression = parseExpression(p);
				expressions.add(expression);

				count++;
			}
			else
				expressions.add(null);
		}
		
		return count;
	}

	protected boolean isEvaluatedProperty(DataSetProperty property)
	{
		return (property.isEvaluated() && !StringUtil.isEmpty(property.getExpression()));
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

	protected Expression parseExpression(DataSetProperty property)
			throws DataSetPropertyExpEvaluatorParseException, DataSetPropertyExpEvaluatorException
	{
		try
		{
			return parseExpression(property.getExpression());
		}
		catch (DataSetPropertyExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetPropertyExpEvaluatorParseException(t, property.getName());
		}
	}

	protected Expression parseExpression(String expression) throws Throwable
	{
		Expression exp = this.expressionParser.parseExpression(expression);

		checkPermission(exp);

		return exp;
	}

	protected void checkPermission(Expression expression) throws Throwable
	{
		if (!(expression instanceof SpelExpression))
			return;

		SpelExpression spelExpression = (SpelExpression) expression;
		SpelNode spelNode = spelExpression.getAST();

		checkSpelNode(spelExpression, spelNode);
	}

	/**
	 * 校验Spel表达式是否合法。
	 * <p>
	 * Spel没有提供更细粒度的表达式控制配置，所以这里通过判断{@linkplain SpelNode}的类型来禁用某些{@linkplain DataSetPropertyExpEvaluator}表达式规范无关的语法，
	 * 以避免安全问题。
	 * </p>
	 * 
	 * @param spelExpression
	 * @param spelNode
	 * @throws Throwable
	 */
	protected void checkSpelNode(SpelExpression spelExpression, SpelNode spelNode) throws Throwable
	{
		if (spelNode == null)
			return;

		boolean illegal = false;

		// 表达式中不允许出现这些语法
		if (spelNode instanceof Assign)
			illegal = true;
		else if (spelNode instanceof BeanReference)
			illegal = true;
		else if (spelNode instanceof ConstructorReference)
			illegal = true;
		else if (spelNode instanceof InlineList)
			illegal = true;
		else if (spelNode instanceof InlineMap)
			illegal = true;
		else if (spelNode instanceof MethodReference)
			illegal = true;
		else if (spelNode instanceof Projection)
			illegal = true;
		else if (spelNode instanceof QualifiedIdentifier)
			illegal = true;
		else if (spelNode instanceof Selection)
			illegal = true;
		else if (spelNode instanceof TypeReference)
			illegal = true;
		
		if(illegal)
			throw new ParseException(spelNode.toStringAST(), spelNode.getStartPosition(), "illegal syntax");

		int cc = spelNode.getChildCount();

		for (int i = 0; i < cc; i++)
		{
			checkSpelNode(spelExpression, spelNode.getChild(i));
		}
	}

	/**
	 * 构建支持此类的表达式语法规范的的计算上下文。
	 * <p>
	 * Spring表达式对于{@linkplain Map}的默认访问语法为：{@code map['key']}，而数据集的结果数据几乎都是{@code Map}类型的，
	 * 这样会导致定义表达式会较繁琐且不够直观。
	 * </p>
	 * <p>
	 * 因而，此方法返回的{@linkplain EvaluationContext}做了特殊处理，除了支持{@code map['key']}的标准语法，
	 * 还支持以{@code map.key}的简化语法访问{@linkplain Map}，从而简化表达式定义。
	 * </p>
	 * <p>
	 * 另外，为了安全性，返回的{@linkplain EvaluationContext}禁用了数据写入、方法调用、类型引用表达式支持。
	 * </p>
	 * 
	 * @return
	 */
	protected EvaluationContext buildEvaluationContext()
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
	 * 计算结果值设置器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface ValueSetter<T>
	{
		/**
		 * 设置计算结果值。
		 * 
		 * @param property
		 * @params propertyIndex
		 * @param data
		 *            待设置{@code value}的数据对象
		 * @param value
		 *            计算结果值
		 */
		void set(DataSetProperty property, int propertyIndex, T data, Object value);
	}
	
	/**
	 * 支持{@linkplain DataSetPropertyExpEvaluator}表达式规范的{@linkplain Map}访问器。
	 * <p>
	 * 此类对{@linkplain Map}的访问规范包括：
	 * </p>
	 * <p>
	 * 1. 支持以{@code map.key}的语法访问关键字值；
	 * </p>
	 * <p>
	 * 2.
	 * 只允许访问{@linkplain Map}的关键字值，不允许访问{@linkplain Map}对象本身的属性（比如{@code size}属性）；
	 * </p>
	 * <p>
	 * 3. 只允许读操作，不允许写操作。
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
