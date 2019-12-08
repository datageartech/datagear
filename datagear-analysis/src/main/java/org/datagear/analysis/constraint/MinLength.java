/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.constraint;

/**
 * 约束-最小长度。
 * 
 * @author datagear@163.com
 *
 */
public class MinLength extends AbstractValueConstraint<Integer>
{
	public MinLength()
	{
		super();
	}

	public MinLength(int value)
	{
		super(value);
	}
}
