/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表达式解析器。
 * <p>
 * 它解析字符串中类似“${content}”、“${name:content}”格式的表达式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ExpressionResolver
{
	public static final String DEFAULT_START_IDENTIFIER = "${";

	public static final String DEFAULT_END_IDENTIFIER = "}";

	public static final String DEFAULT_SEPARATOR = ":";

	public static final char DEFAULT_ESCAPER = '\\';

	private String startIdentifier = DEFAULT_START_IDENTIFIER;

	private String endIdentifier = DEFAULT_END_IDENTIFIER;

	private String separator = DEFAULT_SEPARATOR;

	private char escaper = DEFAULT_ESCAPER;

	private transient char[] _startIdentifierChars = DEFAULT_START_IDENTIFIER.toCharArray();

	private transient char[] _endIdentifierChars = DEFAULT_END_IDENTIFIER.toCharArray();

	private transient char[] _separatorChars = DEFAULT_SEPARATOR.toCharArray();

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
		this._startIdentifierChars = startIdentifier.toCharArray();
	}

	public String getEndIdentifier()
	{
		return endIdentifier;
	}

	public void setEndIdentifier(String endIdentifier)
	{
		this.endIdentifier = endIdentifier;
		this._endIdentifierChars = endIdentifier.toCharArray();
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
		this._separatorChars = separator.toCharArray();
	}

	/**
	 * 判断字符串是否是表达式字符串。
	 * 
	 * @param source
	 * @return
	 */
	public boolean isExpression(String source)
	{
		if (source == null || source.isEmpty())
			return false;

		char[] cs = source.toCharArray();

		return (resolveNextExpression(cs, 0) != null);
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

	/**
	 * 解析对象中的{@linkplain Expression}列表。
	 * <p>
	 * 如果对象不是字符串，将返回空列表。
	 * </p>
	 * 
	 * @param source
	 * @return
	 */
	public List<Expression> resolve(Object source)
	{
		if (source == null || !(source instanceof String))
			return Collections.emptyList();
		else
			return resolve((String) source);

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
		List<Expression> expressions = new ArrayList<Expression>();

		resolve(source, expressions);

		return expressions;
	}

	/**
	 * 计算字符串表达式的值。
	 * 
	 * @param source
	 * @param expressions
	 * @param values
	 * @param nullValue
	 * @return
	 */
	public String evaluate(String source, List<Expression> expressions, List<?> values, String nullValue)
	{
		if (expressions == null || expressions.isEmpty())
			return source;

		StringBuilder result = new StringBuilder();

		int gapStart = 0;

		char[] cs = source.toCharArray();
		for (int i = 0; i < expressions.size(); i++)
		{
			Expression expression = expressions.get(i);
			Object value = values.get(i);

			for (; gapStart < expression.getStart(); gapStart++)
				result.append(cs[gapStart]);

			if (value == null)
				result.append(nullValue);
			else if (value instanceof String)
				result.append((String) value);
			else
				result.append(value.toString());

			gapStart = expression.getEnd();
		}

		for (; gapStart < cs.length; gapStart++)
			result.append(cs[gapStart]);

		return result.toString();
	}

	/**
	 * 解析源字符串中的{@linkplain Expression}，并写入给定列表。
	 * 
	 * @param source
	 * @param expressions
	 */
	protected void resolve(String source, List<Expression> expressions)
	{
		if (source == null || source.isEmpty())
			return;

		char[] cs = source.toCharArray();

		Expression next = null;
		int startIndex = 0;

		while ((next = resolveNextExpression(cs, startIndex)) != null)
		{
			expressions.add(next);
			startIndex = next.getEnd();
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
	protected Expression resolveNextExpression(char[] source, int startIndex)
	{
		for (int i = startIndex; i < source.length; i++)
		{
			char c = source[i];

			if (c == escaper)
			{
				// 下一个字符为待转义字符，跳过
				i += 1;
			}
			else if (c == this._startIdentifierChars[0])
			{
				boolean isStartIdentifier = match(source, i + 1, this._startIdentifierChars, 1);

				if (isStartIdentifier)
				{
					StringBuilder first = new StringBuilder();
					StringBuilder second = null;

					int j = i + this._startIdentifierChars.length;
					for (; j < source.length; j++)
					{
						char cj = source[j];

						if (cj == escaper)
						{
							if (cj < source.length - 1)
							{
								if (second != null)
									second.append(cj);
								else
									first.append(cj);
							}

							j += 1;
						}
						else if (cj == this._separatorChars[0] && second == null
								&& match(source, j + 1, this._separatorChars, 1))
						{
							j += this._separatorChars.length - 1;
							second = new StringBuilder();
						}
						else if (cj == this._endIdentifierChars[0] && match(source, j + 1, this._endIdentifierChars, 1))
						{
							break;
						}
						else
						{
							if (second != null)
								second.append(cj);
							else
								first.append(cj);
						}
					}

					if (j == source.length || first.length() == 0 || (second != null && second.length() == 0))
						continue;
					else
					{
						String name = null, content = null;

						if (second == null)
							content = first.toString().trim();
						else
						{
							name = first.toString().trim();
							content = second.toString().trim();
						}

						return new Expression(new String(source, i, j + 1 - i), i, j + 1, name, content);
					}
				}
			}
			else
				;
		}

		return null;
	}

	/**
	 * 是否匹配前缀。
	 * 
	 * @param source
	 * @param sourceStartIndex
	 * @param prefix
	 * @param prefixStartIndex
	 * @return
	 */
	protected boolean match(char[] source, int sourceStartIndex, char[] prefix, int prefixStartIndex)
	{
		for (int i = sourceStartIndex, j = prefixStartIndex; i < source.length && j < prefix.length; i++, j++)
		{
			if (source[i] != prefix[j])
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * 表达式。
	 * <p>
	 * 格式为：#{name:content}、#{content}
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Expression implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 表达式字符串 */
		private String expression;

		/** 表达式起始位置 */
		private int start;

		/** 表达式结束位置 */
		private int end;

		/** 表达式的名称 */
		private String name;

		/** 表达式内容 */
		private String content;

		public Expression(String expression, int start, int end, String content)
		{
			super();
			this.expression = expression;
			this.start = start;
			this.end = end;
			this.content = content;
		}

		public Expression(String expression, int start, int end, String name, String content)
		{
			super();
			this.expression = expression;
			this.start = start;
			this.end = end;
			this.name = name;
			this.content = content;
		}

		public String getExpression()
		{
			return expression;
		}

		protected void setExpression(String expression)
		{
			this.expression = expression;
		}

		public int getStart()
		{
			return start;
		}

		protected void setStart(int start)
		{
			this.start = start;
		}

		public int getEnd()
		{
			return end;
		}

		protected void setEnd(int end)
		{
			this.end = end;
		}

		/**
		 * 是否有名称。
		 * 
		 * @return
		 */
		public boolean hasName()
		{
			return (this.name != null && !this.name.isEmpty());
		}

		public String getName()
		{
			return name;
		}

		protected void setName(String name)
		{
			this.name = name;
		}

		public String getContent()
		{
			return content;
		}

		protected void setContent(String content)
		{
			this.content = content;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [expression=" + expression + ", start=" + start + ", end=" + end
					+ ", name=" + name + ", content=" + content + "]";
		}
	}
}
