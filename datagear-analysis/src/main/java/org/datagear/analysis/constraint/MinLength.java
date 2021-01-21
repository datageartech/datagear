/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
