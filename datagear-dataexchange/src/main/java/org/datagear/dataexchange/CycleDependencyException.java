/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 指定{@linkplain SubDataExchange}存在循环依赖异常。
 * 
 * @author datagear@163.com
 *
 */
public class CycleDependencyException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private SubDataExchange subDataExchange;

	public CycleDependencyException(SubDataExchange subDataExchange)
	{
		super();
		this.subDataExchange = subDataExchange;
	}

	public CycleDependencyException(SubDataExchange subDataExchange, String message)
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
