/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support.expression;

/**
 * 用于变量表达式求值的Bean。
 * 
 * @author datagear@163.com
 *
 */
public class VariableExpressionBean
{
	private int index;

	public VariableExpressionBean()
	{
		super();
	}

	public VariableExpressionBean(int index)
	{
		super();
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}
}
