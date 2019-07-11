/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.util.expression;

import java.io.Serializable;

/**
 * 表达式。
 * <p>
 * 此类描述嵌入在字符串中的某个表达式子串。
 * </p>
 * <p>
 * 通常格式为：${...}、#{...}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class Expression implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 表达式字符串 */
	private String expression;

	/** 起始标识符 */
	private String startIdentifier;

	/** 结束标识符 */
	private String endIdentifier;

	/** 表达式起始位置 */
	private int startIndex;

	/** 表达式结束位置 */
	private int endIndex;

	/** 表达式内容 */
	private String content;

	public Expression()
	{
		super();
	}

	public Expression(String startIdentifier, String endIdentifier, String expression, int startIndex, int endIndex,
			String content)
	{
		super();
		this.startIdentifier = startIdentifier;
		this.endIdentifier = endIdentifier;
		this.expression = expression;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.content = content;
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

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public int getStartIndex()
	{
		return startIndex;
	}

	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	public int getEndIndex()
	{
		return endIndex;
	}

	public void setEndIndex(int endIndex)
	{
		this.endIndex = endIndex;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return this.expression;
	}
}