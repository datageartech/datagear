/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;

/**
 * 宏计算异常。
 * 
 * @author datagear@163.com
 *
 */
public class MacroEvalException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private String macro;

	public MacroEvalException(String macro)
	{
		super();
		this.macro = macro;
	}

	public MacroEvalException(String macro, String message)
	{
		super(message);
		this.macro = macro;
	}

	public MacroEvalException(String macro, Throwable cause)
	{
		super(cause);
		this.macro = macro;
	}

	public MacroEvalException(String macro, String message, Throwable cause)
	{
		super(message, cause);
		this.macro = macro;
	}

	public String getMacro()
	{
		return macro;
	}

	protected void setMacro(String macro)
	{
		this.macro = macro;
	}
}
