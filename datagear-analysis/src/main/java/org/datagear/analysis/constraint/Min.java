/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.constraint;

/**
 * 约束-最小值。
 * 
 * @author datagear@163.com
 *
 */
public class Min extends AbstractValueConstraint<Number>
{
	public Min()
	{
		super();
	}

	public Min(Number value)
	{
		super(value);
	}
}
