/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
