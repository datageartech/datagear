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

import org.datagear.util.resource.ConnectionFactory;

/**
 * 文本值导入。
 * <p>
 * 导入数据源为字段名称-文本字段值集合，比如：CSV、JSON、EXCEL等。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextValueDataImport extends FormatDataExchange
{
	private ValueDataImportOption importOption;

	private ValueDataImportListener listener;

	public TextValueDataImport()
	{
		super();
	}

	public TextValueDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption)
	{
		super(connectionFactory, dataFormat);
		this.importOption = importOption;
	}

	public ValueDataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(ValueDataImportOption importOption)
	{
		this.importOption = importOption;
	}

	public void setListener(ValueDataImportListener listener)
	{
		this.listener = listener;
	}

	@Override
	public ValueDataImportListener getListener()
	{
		return this.listener;
	}
}
