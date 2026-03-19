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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetField;
import org.datagear.util.StringUtil;
import org.datagear.util.spel.BaseSpelExpressionParser;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * {@linkplain DataSetField#getExpression()}表达式计算器。
 * <p>
 * 此类用于计算{@linkplain DataSet#getFields()}的{@linkplain DataSetField#getExpression()}表达式的值，支持诸如：
 * </p>
 * <p>
 * <code>"(字段名A + 字段名B)*2 - 字段名C/3"</code>
 * </p>
 * <p>
 * 的表达式计算。
 * </p>
 * <p>
 * 表达式语法规范：
 * </p>
 * <p>
 * <ol>
 * <li>字段值：<br>
 * {@linkplain Map}简化语法：{@code 字段名}、{@code 字段名.字段名}<br>
 * {@linkplain Map}标准语法：{@code ['字段名']}、{@code ["字段名"]}、{@code 字段名['字段名']}、{@code 字段名["字段名"]}<br>
 * {@code JavaBean}：{@code 字段名}、{@code 字段名.字段名}<br>
 * {@linkplain List}、数组：{@code [索引数值]}、{@code 字段名[索引数值]}<br>
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
 * 对于{@linkplain #eval(List, List, FieldValueAccessor)}方法，
 * 表达式中的{@code 字段名}可以是{@code fields}列表中任意的{@linkplain DataSetField#getName()}，取值规范如下所示：
 * </p>
 * <p>
 * <ol>
 * <li>非计算字段：{@code datas}元素对象的对应字段值<br>
 * </li>
 * <li>自身字段：{@code datas}元素对象的对应原始字段值<br>
 * </li>
 * <li>前置计算字段：字段表达式计算结果值<br>
 * </li>
 * <li>后置计算字段：{@code datas}元素对象的对应原始字段值<br>
 * </li>
 * </ol>
 * </p>
 * <p>
 * 表达式示例：
 * </p>
 * <p>
 * {@code "字段名A >= 字段名B ? (字段名C + 字段名D)/2 : (字段名E + 字段名F)*2"}
 * </p>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFieldExpEvaluator
{
	public static final DataSetFieldExpEvaluator DEFAULT = new DataSetFieldExpEvaluator();

	private BaseSpelExpressionParser spelExpressionParser = BaseSpelExpressionParser.DEFAULT;

	private ConversionService conversionService = null;

	public DataSetFieldExpEvaluator()
	{
		super();
	}

	public BaseSpelExpressionParser getSpelExpressionParser()
	{
		return spelExpressionParser;
	}

	public void setSpelExpressionParser(BaseSpelExpressionParser spelExpressionParser)
	{
		this.spelExpressionParser = spelExpressionParser;
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
	 * @throws DataSetFieldExpEvaluatorException
	 */
	public Object eval(String expression, Object data) throws DataSetFieldExpEvaluatorException
	{
		try
		{
			Expression exp = this.spelExpressionParser.parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalSingle(exp, context, data);
		}
		catch (DataSetFieldExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetFieldExpEvaluatorException(t);
		}
	}

	/**
	 * 计算表达式的值。
	 * 
	 * @param expression
	 *            {@linkplain DataSetField#getExpression()}不应为空
	 * @param datas
	 * @return
	 * @throws DataSetFieldExpEvaluatorException
	 */
	public Object[] eval(String expression, Object[] datas) throws DataSetFieldExpEvaluatorException
	{
		try
		{
			Expression exp = this.spelExpressionParser.parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalArray(exp, context, datas);
		}
		catch (DataSetFieldExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetFieldExpEvaluatorException(t);
		}
	}

	/**
	 * 计算表达式的值。
	 * 
	 * @param expression
	 * @param datas
	 * @return
	 * @throws DataSetFieldExpEvaluatorException
	 */
	public List<Object> eval(String expression, List<?> datas) throws DataSetFieldExpEvaluatorException
	{
		try
		{
			Expression exp = this.spelExpressionParser.parseExpression(expression);

			EvaluationContext context = buildEvaluationContext();
			return doEvalList(exp, context, datas);
		}
		catch (DataSetFieldExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetFieldExpEvaluatorException(t);
		}
	}

	/**
	 * 统一计算和设置{@linkplain DataSetField#getExpression()}的值。
	 * <p>
	 * 这里统一处理表达式计算，使得{@linkplain DataSetField#getExpression()}可具有更灵活的支持规范，
	 * 具体参考此类说明：{@linkplain DataSetFieldExpEvaluator}。
	 * </p>
	 * 
	 * @param data
	 * @param fields
	 * @param fieldValueAccessor
	 * @throws DataSetFieldExpEvaluatorException
	 */
	public void eval(Object data, List<DataSetField> fields, FieldValueAccessor fieldValueAccessor)
			throws DataSetFieldExpEvaluatorException
	{
		if (data == null || fields == null)
			return;

		EvaluationContext context = buildEvaluationContext();
		eval(data, fields, fieldValueAccessor, context);
	}

	protected void eval(Object data, List<DataSetField> fields, FieldValueAccessor fieldValueAccessor, EvaluationContext context)
			throws DataSetFieldExpEvaluatorException
	{
		if (data == null || fields == null)
			return;

		int fieldLen = fields.size();
		List<Expression> expressions = new ArrayList<Expression>(fieldLen);
		parseExpressions(fields, expressions);

		if (data instanceof Collection<?>)
		{
			Collection<?> datas = (Collection<?>) data;

			for (Object row : datas)
				evalRow(row, fields, fieldValueAccessor, context, expressions);
		}
		else if (data instanceof Object[])
		{
			Object[] datas = (Object[]) data;

			for (Object row : datas)
				evalRow(row, fields, fieldValueAccessor, context, expressions);
		}
		else
			evalRow(data, fields, fieldValueAccessor, context, expressions);
	}

	protected void evalRow(Object row, List<DataSetField> fields, FieldValueAccessor fieldValueAccessor,
			EvaluationContext context, List<Expression> expressions) throws DataSetFieldExpEvaluatorException
	{
		if (row == null || fields == null)
			return;

		DataSetField field = null;

		try
		{
			// 必须按顺序计算，确保表达式中的字段取值逻辑符合规范
			for (int i = 0, len = fields.size(); i < len; i++)
			{
				field = fields.get(i);
				Expression expression = expressions.get(i);
				Object value = null;

				if (expression != null)
				{
					value = doEvalSingle(expression, context, row);
					fieldValueAccessor.set(row, field, value);
				}

				if (DataSetField.DataType.isObjectType(field))
				{
					if (value == null)
						value = fieldValueAccessor.get(row, field);

					eval(value, field.getFields(), fieldValueAccessor, context);
				}
			}
		}
		catch (DataSetFieldExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetFieldExpEvaluatorException(t, (field == null ? "" : field.getName()));
		}
	}

	/**
	 * 解析表达式对象。
	 * 
	 * @param fields
	 * @param expressions
	 *            用于写入表达式的列表，如果某个元素为{@code null}，表示不是计算字段
	 * @returns 计算字段的个数
	 * @throws DataSetFieldExpEvaluatorException
	 */
	protected int parseExpressions(List<DataSetField> fields, List<Expression> expressions)
			throws DataSetFieldExpEvaluatorException
	{
		int count = 0;
		
		for(DataSetField p : fields)
		{
			if (isEvaluatedField(p))
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

	protected boolean isEvaluatedField(DataSetField field)
	{
		return (field.isEvaluated() && !StringUtil.isEmpty(field.getExpression()));
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
		return this.spelExpressionParser.getValue(expression, context, data);
	}

	protected Expression parseExpression(DataSetField field)
			throws DataSetFieldExpEvaluatorParseException, DataSetFieldExpEvaluatorException
	{
		try
		{
			return this.spelExpressionParser.parseExpression(field.getExpression());
		}
		catch (DataSetFieldExpEvaluatorException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetFieldExpEvaluatorParseException(t, field.getName());
		}
	}

	protected EvaluationContext buildEvaluationContext()
	{
		return this.spelExpressionParser.readonlyMapSimplifyContext(this.conversionService);
	}

	/**
	 * 字段值访问器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface FieldValueAccessor
	{
		/**
		 * 设置计算结果值。
		 * 
		 * @param row
		 *            待设置{@code value}的数据对象
		 * @param field
		 * @param value
		 *            计算结果值
		 */
		void set(Object row, DataSetField field, Object value);

		/**
		 * 获取字段值。
		 * 
		 * @param row
		 * @param field
		 * @return
		 */
		Object get(Object row, DataSetField field);
	}
}
