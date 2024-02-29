/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.persistence.support.expression;

import java.util.Collections;
import java.util.List;

import org.datagear.util.expression.ExpressionResolver;

/**
 * {@linkplain NameExpression}解析器。
 * 
 * @author datagear@163.com
 *
 */
public class NameExpressionResolver extends ExpressionResolver
{
	public static final String DEFAULT_SEPARATOR = ":";

	private String separator = DEFAULT_SEPARATOR;

	public NameExpressionResolver()
	{
		super();
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	/**
	 * 解析{@linkplain NameExpression}。
	 * <p>
	 * 如果不包含表达式，将返回空列表。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public List<NameExpression> resolveNameExpressions(String source)
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<NameExpression> expressions = (List) super.resolve(source);

		return expressions;
	}

	/**
	 * 解析{@linkplain NameExpression}。
	 * <p>
	 * 如果不包含表达式，将返回空列表。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public List<NameExpression> resolveNameExpressions(Object source)
	{
		if (source == null || !(source instanceof String))
			return Collections.emptyList();
		else
			return resolveNameExpressions((String) source);
	}

	/**
	 * 解析第一个{@linkplain NameExpression}。
	 * 
	 * @param source
	 * @return
	 */
	public NameExpression resolveFirstNameExpression(String source)
	{
		return (NameExpression) super.resolveFirst(source);
	}

	/**
	 * 判断对象是否是表达式字符串。
	 * <p>
	 * 如果对象不是字符串，将返回{@code false}。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public boolean isExpression(Object source)
	{
		if (source == null || !(source instanceof String))
			return false;

		return isExpression((String) source);
	}

	@Override
	protected NameExpression newExpressionInstance(String startIdentifier, String endIdentifier, String expression,
			int startIndex, int endIndex, String content)
	{
		NameExpression nameExpression = new NameExpression(startIdentifier, endIdentifier, expression, startIndex,
				endIndex, content);

		String[] nameAndContent = resolveNameAndContent(content);

		if (nameAndContent.length == 1)
			nameExpression.setContent(nameAndContent[0]);
		else
		{
			nameExpression.setName(nameAndContent[0]);
			nameExpression.setContent(nameAndContent[1]);
		}

		return nameExpression;
	}

	/**
	 * 解析名字内容数组。
	 * 
	 * @param source
	 * @return
	 */
	protected String[] resolveNameAndContent(String source)
	{
		StringBuilder first = new StringBuilder();
		StringBuilder second = null;

		for (int i = 0; i < source.length();)
		{
			char c = source.charAt(i);
			if (c == getEscaper() && matchAtIndex(source, i + 1, this.separator))
			{
				if (second != null)
					second.append(this.separator);
				else
					first.append(this.separator);

				i += this.separator.length() + 1;
			}
			else if (matchAtIndex(source, i, this.separator))
			{
				i += this.separator.length();
				second = new StringBuilder();
			}
			else
			{
				if (second != null)
					second.append(c);
				else
					first.append(c);

				i += 1;
			}
		}

		String firstStr = first.toString().trim();
		String secondStr = (second == null ? null : second.toString().trim());

		if (secondStr == null || secondStr.isEmpty())
			return new String[] { firstStr };
		else
			return new String[] { firstStr, secondStr };
	}
}
