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

package org.datagear.util.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datagear.util.StringUtil;

/**
 * 字符串表达式处理器。
 * <p>
 * 它解析字符串中类似<code>"${...}"</code>、<code>"#{...}"</code>格式的表达式子串，并可进行相关的求值、提取处理。
 * </p>
 * <p>
 * 默认地，它使用<code>'\'</code>作为转义字符。
 * </p>
 * <p>
 * 例如：<code>"\${"</code>表示不做表达式解析、表达式里面的<code>"\}"</code>表示<code>"}"</code>普通字符而非表达式结束标识符。
 * </p>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ExpressionResolver
{
	/** "${"起始标识符 */
	public static final String DEFAULT_START_IDENTIFIER_DOLLAR = "${";

	/** "#{"起始标识符 */
	public static final String DEFAULT_START_IDENTIFIER_SHARP = "#{";

	public static final String DEFAULT_END_IDENTIFIER = "}";

	public static final char DEFAULT_ESCAPER = '\\';

	private String startIdentifier = DEFAULT_START_IDENTIFIER_DOLLAR;

	private String endIdentifier = DEFAULT_END_IDENTIFIER;

	private char escaper = DEFAULT_ESCAPER;

	public ExpressionResolver()
	{
		super();
	}

	public String getStartIdentifier()
	{
		return startIdentifier;
	}

	public void setStartIdentifier(String startIdentifier)
	{
		this.startIdentifier = startIdentifier;
	}

	public String getEndIdentifier()
	{
		return endIdentifier;
	}

	public void setEndIdentifier(String endIdentifier)
	{
		this.endIdentifier = endIdentifier;
	}

	public char getEscaper()
	{
		return escaper;
	}

	public void setEscaper(char escaper)
	{
		this.escaper = escaper;
	}

	/**
	 * 判断字符串是否是表达式字符串。
	 * 
	 * @param source
	 * @return
	 */
	public boolean isExpression(String source)
	{
		if (StringUtil.isEmpty(source))
			return false;

		return (resolveNextExpression(source, 0) != null);
	}

	/**
	 * 是否是严格的表达式字符串。
	 * <p>
	 * 严格表达式字符串以{@linkplain #getStartIdentifier()}开头，以{@linkplain #getEndIdentifier()}结尾。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public boolean isExpressionStrict(String source)
	{
		if (StringUtil.isEmpty(source))
			return false;

		return source.startsWith(this.startIdentifier) && source.endsWith(this.endIdentifier);
	}

	/**
	 * 指定{@code expression}是否是严格表达式。
	 * 
	 * @param source
	 * @param expression
	 * @return
	 */
	public boolean isExpressionStrict(String source, Expression expression)
	{
		return expression.getStartIndex() == 0 && expression.getEndIndex() == source.length();
	}

	/**
	 * 解析字符串中的{@linkplain Expression}列表。
	 * <p>
	 * 如果不包含表达式，将返回空列表。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public List<Expression> resolve(String source)
	{
		if (source == null || source.isEmpty())
			return Collections.emptyList();

		List<Expression> expressions = new ArrayList<Expression>(3);

		Expression next = null;
		int startIndex = 0;

		while ((next = resolveNextExpression(source, startIndex)) != null)
		{
			expressions.add(next);
			startIndex = next.getEndIndex();
		}

		return expressions;
	}

	/**
	 * 解析第一个表达式。
	 * <p>
	 * 如果没有，返回{@code null}。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public Expression resolveFirst(String source)
	{
		if (source == null || source.isEmpty())
			return null;

		return resolveNextExpression(source, 0);
	}

	/**
	 * 解析下一个表达式。
	 * <p>
	 * 如果没有，返回{@code null}。
	 * </p>
	 * 
	 * @param source
	 * @param index
	 * @return
	 */
	public Expression resolveNext(String source, int index)
	{
		if (source == null || source.isEmpty())
			return null;

		return resolveNextExpression(source, index);
	}

	/**
	 * 计算字符串表达式的值。
	 * <p>
	 * 将字符串中的表达式子串替换为目标值，并返回替换后的新字符串。
	 * </p>
	 * 
	 * @param source
	 * @param expression
	 * @param value
	 * @param nullValue
	 * @return
	 */
	public String evaluate(String source, Expression expression, Object value, String nullValue)
	{
		return evaluate(source, Arrays.asList(expression), Arrays.asList(value), nullValue);
	}

	/**
	 * 计算字符串表达式的值。
	 * <p>
	 * 将字符串中的表达式子串替换为目标值，并返回替换后的新字符串。
	 * </p>
	 * 
	 * @param source
	 * @param expressions
	 * @param values
	 *            允许元素为{@code null}
	 * @param nullValue
	 * @return
	 */
	public String evaluate(String source, List<? extends Expression> expressions, List<?> values, String nullValue)
	{
		if (expressions == null || expressions.isEmpty())
			return source;

		StringBuilder result = new StringBuilder();

		int gapStart = 0;

		for (int i = 0; i < expressions.size(); i++)
		{
			Expression expression = expressions.get(i);
			Object value = values.get(i);

			copyForUnescape(source, gapStart, expression.getStartIndex(), result);

			if (value == null)
				result.append(nullValue);
			else if (value instanceof String)
				result.append((String) value);
			else
				result.append(value.toString());

			gapStart = expression.getEndIndex();
		}

		copyForUnescape(source, gapStart, source.length(), result);

		return result.toString();
	}

	/**
	 * 根据表达式模板与其包含的表达式列表，解析对应字符串值中的表达式值。
	 * <p>
	 * 如果没有表达式值，返回{@code null}。
	 * </p>
	 * 
	 * @param template
	 * @param expressions
	 * @param value
	 * @return
	 */
	public String extract(String template, Expression expressions, String value)
	{
		List<String> re = extract(template, Arrays.asList(expressions), value);

		if (re == null || re.isEmpty())
			return null;

		return re.get(0);
	}

	/**
	 * 根据表达式模板与其包含的表达式列表，解析对应字符串值中的表达式值。
	 * <p>
	 * 返回列表的长度有可能小于表达式列表的长度。
	 * </p>
	 * 
	 * @param template
	 * @param expressions
	 * @param value
	 * @return
	 */
	public List<String> extract(String template, List<? extends Expression> expressions, String value)
	{
		int expSize = (expressions == null ? 0 : expressions.size());

		if (expSize == 0)
			return Collections.emptyList();

		List<String> expValues = new ArrayList<String>(3);

		int currentExpIdx = 0;
		StringBuilder cache = new StringBuilder();

		for (int i = expressions.get(0).getStartIndex(), len = value.length(); i < len;)
		{
			if (currentExpIdx >= expSize)
				break;

			Expression exp = expressions.get(currentExpIdx);
			Expression nextExp = (currentExpIdx + 1 >= expSize ? null : expressions.get(currentExpIdx + 1));

			String nextGapStr = null;

			if (nextExp == null)
			{
				if (exp.getEndIndex() < template.length())
					nextGapStr = template.substring(exp.getEndIndex());
			}
			else
				nextGapStr = template.substring(exp.getEndIndex(), nextExp.getStartIndex());

			String expValue;

			if (nextGapStr == null || nextGapStr.isEmpty())
			{
				expValue = value.substring(i);
				i = len;
			}
			else
			{
				for (; i < len;)
				{
					if (matchAtIndex(value, i, nextGapStr))
					{
						i += nextGapStr.length();
						break;
					}
					else
					{
						cache.append(value.charAt(i));
						i += 1;
					}
				}

				expValue = cache.toString();
				cache.delete(0, cache.length());
			}

			expValues.add(expValue);
			currentExpIdx += 1;
		}

		return expValues;
	}

	/**
	 * 将不包含表达式的字符串进行表达式语法反转义。
	 * <p>
	 * 注意：它仅将表达式起始标识符进行反转义。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public String unescape(String source)
	{
		if (source == null || source.isEmpty())
			return source;

		StringBuilder sb = new StringBuilder();

		copyForUnescape(source, 0, source.length(), sb);

		return sb.toString();
	}

	/**
	 * 将对象进行表达式语法反转义。
	 * <p>
	 * 如果{@code obj}不是字符串，将直接返回它。
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	public Object unescape(Object obj)
	{
		if (obj instanceof String)
			return unescape((String) obj);
		else
			return obj;
	}

	/**
	 * 将源字符串中指定索引区间的字符拷贝到目标字符串缓冲，并进行表达式语法反转义。
	 * 
	 * @param source
	 * @param startIndex
	 * @param endIndex
	 * @param sb
	 */
	protected void copyForUnescape(String source, int startIndex, int endIndex, StringBuilder sb)
	{
		for (int i = startIndex; i < endIndex; i++)
		{
			char c = source.charAt(i);

			if (c == this.escaper && matchAtIndex(source, i + 1, this.startIdentifier))
				;
			else
				sb.append(c);
		}
	}

	/**
	 * 从给定起始位置解析下一个{@linkplain Expression}。
	 * <p>
	 * 如果没有，将返回{@code null}。
	 * </p>
	 * 
	 * @param source
	 * @param startIndex
	 * @return
	 */
	protected Expression resolveNextExpression(String source, int startIndex)
	{
		int length = source.length();

		for (int i = startIndex; i < length;)
		{
			// 起始标识符转义
			if (source.charAt(i) == this.escaper && matchAtIndex(source, i + 1, this.startIdentifier))
			{
				i += this.startIdentifier.length() + 1;
			}
			else if (matchAtIndex(source, i, this.startIdentifier))
			{
				StringBuilder content = new StringBuilder();

				int j = i + this.startIdentifier.length();

				while (j < length)
				{
					char cj = source.charAt(j);

					// 结束标识符转义
					if (cj == this.escaper && matchAtIndex(source, j + 1, this.endIdentifier))
					{
						content.append(this.endIdentifier);
						j += this.endIdentifier.length() + 1;
					}
					else if (matchAtIndex(source, j, this.endIdentifier))
					{
						break;
					}
					else
					{
						content.append(cj);
						j += 1;
					}
				}

				if (j >= length || content.length() == 0)
				{
					i = j + 1;
					continue;
				}
				else
				{
					return newExpressionInstance(this.startIdentifier, this.endIdentifier, source.substring(i, j + 1),
							i, j + 1, content.toString().trim());
				}
			}
			else
				i += 1;
		}

		return null;
	}

	/**
	 * 判断字符串指定位置是否是目标子串。
	 * 
	 * @param source
	 * @param startIndex
	 * @param sub
	 * @return
	 */
	protected boolean matchAtIndex(String source, int startIndex, String sub)
	{
		if (startIndex >= source.length())
			return false;

		return source.startsWith(sub, startIndex);
	}

	/**
	 * 创建{@linkplain Expression}实例。
	 * 
	 * @param startIdentifier
	 * @param endIdentifier
	 * @param expression
	 * @param startIndex
	 * @param endIndex
	 * @param content
	 * @return
	 */
	protected Expression newExpressionInstance(String startIdentifier, String endIdentifier, String expression,
			int startIndex, int endIndex, String content)
	{
		return new Expression(startIdentifier, endIdentifier, expression, startIndex, endIndex, content);
	}
}
