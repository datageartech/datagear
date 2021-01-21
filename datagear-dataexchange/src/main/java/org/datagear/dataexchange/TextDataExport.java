/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
