/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本值导入设置项。
 * 
 * @author datagear@163.com
 *
 */
public class TextValueDataImportOption extends DataImportOption
{
	private static final long serialVersionUID = 1L;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	/** 当列值非法时设置为null */
	private boolean nullForIllegalColumnValue;

	public TextValueDataImportOption()
	{
		super();
	}

	public TextValueDataImportOption(ExceptionResolve exceptionResolve, boolean ignoreInexistentColumn,
			boolean nullForIllegalColumnValue)
	{
		super(exceptionResolve);
		this.ignoreInexistentColumn = ignoreInexistentColumn;
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
		return getClass().getSimpleName() + " [exceptionResolve=" + getExceptionResolve() + ", ignoreInexistentColumn="
				+ ignoreInexistentColumn + ", nullForIllegalColumnValue=" + nullForIllegalColumnValue + "]";
	}

}
