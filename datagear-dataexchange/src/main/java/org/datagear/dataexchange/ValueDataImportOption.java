/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.dataexchange;

/**
 * 值导入设置项。
 * 
 * @author datagear@163.com
 *
 */
public class ValueDataImportOption extends DataImportOption
{
	private static final long serialVersionUID = 1L;

	/** 是否忽略不存在的列 */
	private boolean ignoreInexistentColumn;

	/** 当列值非法时设置为null */
	private boolean nullForIllegalColumnValue;

	public ValueDataImportOption()
	{
		super();
	}

	public ValueDataImportOption(ExceptionResolve exceptionResolve, boolean ignoreInexistentColumn,
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
