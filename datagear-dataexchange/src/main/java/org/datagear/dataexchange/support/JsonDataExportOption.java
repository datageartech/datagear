/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.TextDataExportOption;

/**
 * JSON导出设置项。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataExportOption extends TextDataExportOption
{
	private static final long serialVersionUID = 1L;

	private JsonDataFormat jsonDataFormat = JsonDataFormat.TABLE_OBJECT;

	private boolean prettyPrint = true;

	public JsonDataExportOption()
	{
		super();
	}

	public JsonDataFormat getJsonDataFormat()
	{
		return jsonDataFormat;
	}

	public void setJsonDataFormat(JsonDataFormat jsonDataFormat)
	{
		this.jsonDataFormat = jsonDataFormat;
	}

	public boolean isPrettyPrint()
	{
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint)
	{
		this.prettyPrint = prettyPrint;
	}
}
