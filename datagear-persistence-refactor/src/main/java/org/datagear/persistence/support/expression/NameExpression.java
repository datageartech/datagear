/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support.expression;

import org.datagear.util.expression.Expression;

/**
 * 名字表达式。
 * <p>
 * 格式为：<code>"${name:content}"</code>、<code>"#{name:content}"</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class NameExpression extends Expression
{
	private static final long serialVersionUID = 1L;

	private String name;

	public NameExpression()
	{
		super();
	}

	public NameExpression(String startIdentifier, String endIdentifier, String expression, int startIndex, int endIndex,
			String content)
	{
		super(startIdentifier, endIdentifier, expression, startIndex, endIndex, content);
	}

	public boolean hasName()
	{
		return (this.name != null && !this.name.isEmpty());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
