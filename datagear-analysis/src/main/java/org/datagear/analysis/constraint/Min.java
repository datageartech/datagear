/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
