/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 指定{@linkplain SubDataExchange}存在循环依赖异常。
 * 
 * @author datagear@163.com
 *
 */
public class CircularDependencyException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private SubDataExchange subDataExchange;

	public CircularDependencyException(SubDataExchange subDataExchange)
	{
		super();
		this.subDataExchange = subDataExchange;
	}

	public CircularDependencyException(SubDataExchange subDataExchange, String message)
	{
		super(message);
		this.subDataExchange = subDataExchange;
	}

	public SubDataExchange getSubDataExchange()
	{
		return subDataExchange;
	}

	protected void setSubDataExchange(SubDataExchange subDataExchange)
	{
		this.subDataExchange = subDataExchange;
	}
}
