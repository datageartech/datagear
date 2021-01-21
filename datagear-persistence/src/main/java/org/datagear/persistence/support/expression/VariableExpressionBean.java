/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
