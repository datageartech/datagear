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
 * 导入数据时设置列值异常。
 * 
 * @author datagear@163.com
 *
 */
public class SetImportColumnValueException extends IndexDataExchangeException
{
	private static final long serialVersionUID = 1L;

	private String columnName;

	private Object sourceValue;

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue)
	{
		super(dataIndex);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message)
	{
		super(dataIndex, message);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, Throwable cause)
	{
		super(dataIndex, cause);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public SetImportColumnValueException(DataIndex dataIndex, String columnName, Object sourceValue, String message,
			Throwable cause)
	{
		super(dataIndex, message, cause);
		this.columnName = columnName;
		this.sourceValue = sourceValue;
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public Object getSourceValue()
	{
		return sourceValue;
	}

	protected void setSourceValue(Object sourceValue)
	{
		this.sourceValue = sourceValue;
	}
}
