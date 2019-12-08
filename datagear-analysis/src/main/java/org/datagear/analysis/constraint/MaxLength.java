/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.constraint;

/**
 * 约束-最大长度。
 * 
 * @author datagear@163.com
 *
 */
public class MaxLength extends AbstractValueConstraint<Integer>
{
	public MaxLength()
	{
		super();
	}

	public MaxLength(int value)
	{
		super(value);
	}
}
