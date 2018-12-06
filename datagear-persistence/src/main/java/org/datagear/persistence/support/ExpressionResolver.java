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
 * 它解析字符串中类似"${content}"、"${name:content}"格式的表达式。
 * </p>
 * <p>
 * 默认地，它使用{@code '\'}作为转义字符，字符串中的"\${"、"\:"、"\}"将作为普通字符。
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

	public static final String DEFAULT_SEPARATOR = ":";

	public static final char DEFAULT_ESCAPER = '\\';

	private String startIdentifier = DEFAULT_START_IDENTIFIER_DOLLAR;

	private String endIdentifier = DEFAULT_END_IDENTIFIER;

	private String separator = DEFAULT_SEPARATOR;

	private char escaper = DEFAULT_ESCAPER;

	private transient char[] _startIdentifierChars = DEFAULT_START_IDENTIFIER_DOLLAR.toCharArray();

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
		if (source == null || source.isEmpty())
			return Collections.emptyList();

		List<Expression> expressions = null;

		char[] cs = source.toCharArray();

		Expression next = null;
		int startIndex = 0;

		while ((next = resolveNextExpression(cs, startIndex)) != null)
		{
			if (expressions == null)
				expressions = new ArrayList<Expression>();

			expressions.add(next);
			startIndex = next.getEnd();
		}

		if (expressions == null)
			expressions = Collections.emptyList();

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

			copyForUnescape(cs, gapStart, expression.getStart(), result);

			if (value == null)
				result.append(nullValue);
			else if (value instanceof String)
				result.append((String) value);
			else
				result.append(value.toString());

			gapStart = expression.getEnd();
		}

		copyForUnescape(cs, gapStart, cs.length, result);

		return result.toString();
	}

	/**
	 * 将字符串进行表达式语法反转义。
	 * <p>
	 * 注意：它仅会反转义匹配表达式起始标识符、分隔标识符、结束标识符的的子串。
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
		char[] cs = source.toCharArray();

		copyForUnescape(cs, 0, cs.length, sb);

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
	 * 将源字符数组中的某些字符拷贝到目标字符串缓冲，并进行表达式语法反转义。
	 * <p>
	 * 注意：为了减少输入时的转义麻烦，它仅会反转义匹配表达式起始标识符、分隔标识符、结束标识符的的字符串。
	 * </p>
	 * 
	 * @param source
	 * @param start
	 * @param end
	 * @param sb
	 */
	protected void copyForUnescape(char[] source, int start, int end, StringBuilder sb)
	{
		for (int i = start; i < end; i++)
		{
			char c = source[i];

			if (c == this.escaper)
			{
				if (match(source, i + 1, this._startIdentifierChars, 0))
					;
				else if (match(source, i + 1, this._separatorChars, 0))
					;
				else if (match(source, i + 1, this._endIdentifierChars, 0))
					;
				else
					sb.append(c);
			}
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
	protected Expression resolveNextExpression(char[] source, int startIndex)
	{
		for (int i = startIndex; i < source.length; i++)
		{
			char pci = (i < 1 ? 0 : source[i - 1]);

			// 是起始标识符并且前一个字符不是转义字符
			if (match(source, i, this._startIdentifierChars, 0) && pci != this.escaper)
			{
				StringBuilder first = new StringBuilder();
				StringBuilder second = null;

				int j = i + this._startIdentifierChars.length;
				for (; j < source.length; j++)
				{
					char cj = source[j];

					if (cj == this.escaper)
					{
						if (match(source, j + 1, this._separatorChars, 0))
						{
							if (second != null)
								second.append(this._separatorChars);
							else
								first.append(this._separatorChars);

							j += this._separatorChars.length;
						}
						else if (match(source, j + 1, this._endIdentifierChars, 0))
						{
							if (second != null)
								second.append(this._endIdentifierChars);
							else
								first.append(this._endIdentifierChars);

							j += this._endIdentifierChars.length;
						}
						else
						{
							if (second != null)
								second.append(this._separatorChars);
							else
								first.append(this._separatorChars);
						}
					}
					else if (match(source, j, this._separatorChars, 0) && second == null)
					{
						j += this._separatorChars.length - 1;
						second = new StringBuilder();
					}
					else if (match(source, j, this._endIdentifierChars, 0))
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

					return new Expression(this.getStartIdentifier(), this.endIdentifier,
							new String(source, i, j + 1 - i), i, j + 1, name, content);
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
	 * 格式为：${name:content}、${content}、#{name:content}、#{content}。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Expression implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 起始标识符 */
		private String startIdentifier;

		/** 结束标识符 */
		private String endIdentifier;

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

		public Expression(String startIdentifier, String endIdentifier, String expression, int start, int end,
				String content)
		{
			super();
			this.startIdentifier = startIdentifier;
			this.endIdentifier = endIdentifier;
			this.expression = expression;
			this.start = start;
			this.end = end;
			this.content = content;
		}

		public Expression(String startIdentifier, String endIdentifier, String expression, int start, int end,
				String name, String content)
		{
			super();
			this.startIdentifier = startIdentifier;
			this.endIdentifier = endIdentifier;
			this.expression = expression;
			this.start = start;
			this.end = end;
			this.name = name;
			this.content = content;
		}

		public String getStartIdentifier()
		{
			return startIdentifier;
		}

		protected void setStartIdentifier(String startIdentifier)
		{
			this.startIdentifier = startIdentifier;
		}

		public String getEndIdentifier()
		{
			return endIdentifier;
		}

		protected void setEndIdentifier(String endIdentifier)
		{
			this.endIdentifier = endIdentifier;
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
