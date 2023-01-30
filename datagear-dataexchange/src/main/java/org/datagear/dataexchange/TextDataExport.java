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
 * 文本导出。
 * <p>
 * 导出为文本，比如：CSV、JSON、EXCEL、SQL等。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TextDataExport extends FormatDataExchange
{
	private TextDataExportOption exportOption;

	private TextDataExportListener listener;

	public TextDataExport()
	{
		super();
	}

	public TextDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption)
	{
		super(connectionFactory, dataFormat);
		this.exportOption = exportOption;
	}

	public TextDataExportOption getExportOption()
	{
		return exportOption;
	}

	public void setExportOption(TextDataExportOption exportOption)
	{
		this.exportOption = exportOption;
	}

	@Override
	public TextDataExportListener getListener()
	{
		return listener;
	}

	public void setListener(TextDataExportListener listener)
	{
		this.listener = listener;
	}
}
