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
 * 约束-必填。
 * 
 * @author datagear@163.com
 *
 */
public class Required extends AbstractValueConstraint<Boolean>
{
	public Required()
	{
		super();
	}

	public Required(boolean value)
	{
		super(value);
	}
}
