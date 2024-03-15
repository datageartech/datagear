/*
 * Copyright 2018-present datagear.tech
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

	/**
	 * 如果列是外键且列值为空字符串时，是否设置为null。
	 * <p>
	 * 某些文本类数据源（比如CSV）本应为{@code null}的值可能会读取为空字符串，
	 * 此时如果对应列为外键列，某些数据库（比如MySQL-5.7）无法处理，需要重新设置为{@code null}才可以。
	 * </p>
	 */
	private boolean nullForEmptyImportKey = false;

	public ValueDataImportOption()
	{
		super();
	}

	public ValueDataImportOption(ExceptionResolve exceptionResolve, boolean ignoreInexistentColumn,
			boolean nullForIllegalColumnValue, boolean nullForEmptyImportKey)
	{
		super(exceptionResolve);
		this.ignoreInexistentColumn = ignoreInexistentColumn;
		this.nullForIllegalColumnValue = nullForIllegalColumnValue;
		this.nullForEmptyImportKey = nullForEmptyImportKey;
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

	public boolean isNullForEmptyImportKey()
	{
		return nullForEmptyImportKey;
	}

	public void setNullForEmptyImportKey(boolean nullForEmptyImportKey)
	{
		this.nullForEmptyImportKey = nullForEmptyImportKey;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [ignoreInexistentColumn=" + ignoreInexistentColumn
				+ ", nullForIllegalColumnValue=" + nullForIllegalColumnValue + ", nullForEmptyImportKey="
				+ nullForEmptyImportKey + ", exceptionResolve=" + getExceptionResolve() + "]";
	}
}
