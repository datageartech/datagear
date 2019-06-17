/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Serializable;

/**
 * 文本导入设置项。
 * 
 * @author datagear@163.com
 *
 */
public class TextDataImportOption implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	/** 导入出错处理方式 */
	private ExceptionResolve exceptionResolve;

	/** 当列值非法时设置为null */
	private boolean nullForIllegalColumnValue;

	public TextDataImportOption()
	{
		super();
	}

	public TextDataImportOption(boolean ignoreInexistentColumn, ExceptionResolve exceptionResolve,
			boolean nullForIllegalColumnValue)
	{
		super();
		this.ignoreInexistentColumn = ignoreInexistentColumn;
		this.exceptionResolve = exceptionResolve;
		this.nullForIllegalColumnValue = nullForIllegalColumnValue;
	}

	public boolean isIgnoreInexistentColumn()
	{
		return ignoreInexistentColumn;
	}

	public void setIgnoreInexistentColumn(boolean ignoreInexistentColumn)
	{
		this.ignoreInexistentColumn = ignoreInexistentColumn;
	}

	public ExceptionResolve getExceptionResolve()
	{
		return exceptionResolve;
	}

	public void setExceptionResolve(ExceptionResolve exceptionResolve)
	{
		this.exceptionResolve = exceptionResolve;
	}

	public boolean isNullForIllegalColumnValue()
	{
		return nullForIllegalColumnValue;
	}

	public void setNullForIllegalColumnValue(boolean nullForIllegalColumnValue)
	{
		this.nullForIllegalColumnValue = nullForIllegalColumnValue;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [ignoreInexistentColumn=" + ignoreInexistentColumn + ", exceptionResolve="
				+ exceptionResolve + ", nullForIllegalColumnValue=" + nullForIllegalColumnValue + "]";
	}
}
