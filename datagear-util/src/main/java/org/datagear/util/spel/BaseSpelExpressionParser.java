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

package org.datagear.util.spel;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
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

/**
 * 基础Spel表达式解析器。
 * <p>
 * 此类只启用了基础的Bean读写特性，避免其他Spel特性引起安全漏洞。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class BaseSpelExpressionParser
{
	public static final BaseSpelExpressionParser INSTANCE = new BaseSpelExpressionParser();

	/**
	 * 表达式解析器。
	 * <p>
	 * 这里考虑安全，明确禁止了它的{@code autoGrowNullReferences}、{@code autoGrowCollections}特性
	 * </p>
	 */
	private SpelExpressionParser spelExpressionParser = new SpelExpressionParser(
			new SpelParserConfiguration(false, false));

	public BaseSpelExpressionParser()
	{
		super();
	}

	public SpelExpressionParser getSpelExpressionParser()
	{
		return spelExpressionParser;
	}

	public void setSpelExpressionParser(SpelExpressionParser spelExpressionParser)
	{
		this.spelExpressionParser = spelExpressionParser;
	}

	/**
	 * 解析表达式。
	 * <p>
	 * 返回前会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @return
	 * @throws ParseException
	 */
	public Expression parseExpression(String expression) throws ParseException
	{
		Expression re = this.spelExpressionParser.parseExpression(expression);
		checkSpelExpression(re);

		return re;
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 此方法会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param root
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(String expression, Object root) throws ParseException, EvaluationException
	{
		Expression exp = parseExpression(expression);
		return exp.getValue(root);
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 注意：此方法不会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param root
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(Expression expression, Object root) throws ParseException, EvaluationException
	{
		return expression.getValue(root);
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 此方法会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param context
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(String expression, EvaluationContext context) throws ParseException, EvaluationException
	{
		Expression exp = parseExpression(expression);
		return exp.getValue(context);
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 注意：此方法不会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param context
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(Expression expression, EvaluationContext context) throws ParseException, EvaluationException
	{
		return expression.getValue(context);
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 此方法会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param context
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(String expression, EvaluationContext context, Object root)
			throws ParseException, EvaluationException
	{
		Expression exp = parseExpression(expression);
		return exp.getValue(context, root);
	}

	/**
	 * 计算表达式。
	 * <p>
	 * 注意：此方法不会校验表达式语法的安全合法性。
	 * </p>
	 * 
	 * @param expression
	 * @param context
	 * @return
	 * @throws ParseException
	 * @throws EvaluationException
	 */
	public Object getValue(Expression expression, EvaluationContext context, Object root)
			throws ParseException, EvaluationException
	{
		return expression.getValue(context, root);
	}

	/**
	 * 校验Spel表达式是否合法。
	 * 
	 * @param expression
	 * @throws ParseException
	 */
	protected void checkSpelExpression(Expression expression) throws ParseException
	{
		if (!(expression instanceof SpelExpression))
			throw new ParseException(expression.getExpressionString(), 0, "illegal syntax");

		SpelExpression spelExpression = (SpelExpression) expression;
		SpelNode spelNode = spelExpression.getAST();

		checkSpelNode(spelExpression, spelNode);
	}

	/**
	 * 校验Spel表达式是否合法。
	 * <p>
	 * Spel没有提供更细粒度的表达式控制配置，所以这里通过判断{@linkplain SpelNode}的类型来禁用某些无关的语法，以避免安全问题。
	 * </p>
	 * 
	 * @param expression
	 * @param spelNode
	 * @throws ParseException
	 */
	protected void checkSpelNode(SpelExpression expression, SpelNode spelNode) throws ParseException
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

		if (illegal)
			throw new ParseException(expression.getExpressionString(), spelNode.getStartPosition(), "illegal syntax");

		int cc = spelNode.getChildCount();

		for (int i = 0; i < cc; i++)
		{
			checkSpelNode(expression, spelNode.getChild(i));
		}
	}
}
