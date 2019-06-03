/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本导入。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataImport extends TextDataExchange
{
	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	/** 导入出错处理方式 */
	private ExceptionResolve exceptionResolve;

	/** 当列值非法时设置为null */
	private boolean nullForIllegalColumnValue;

	public TextDataImport()
	{
		super();
	}

	public TextDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, boolean ignoreInexistentColumn,
			ExceptionResolve exceptionResolve, boolean nullForIllegalColumnValue)
	{
		super(connectionFactory, dataFormat);
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
}
