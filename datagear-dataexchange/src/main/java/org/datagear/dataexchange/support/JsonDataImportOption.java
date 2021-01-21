/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ValueDataImportOption;

/**
 * JSON数据导入设置项。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataImportOption extends ValueDataImportOption
{
	private static final long serialVersionUID = 1L;

	private JsonDataFormat jsonDataFormat = JsonDataFormat.TABLE_OBJECT;

	public JsonDataImportOption()
	{
	}

	public JsonDataImportOption(ExceptionResolve exceptionResolve, boolean ignoreInexistentColumn,
			boolean nullForIllegalColumnValue, JsonDataFormat jsonDataFormat)
	{
		super(exceptionResolve, ignoreInexistentColumn, nullForIllegalColumnValue);
		this.jsonDataFormat = jsonDataFormat;
	}

	public JsonDataFormat getJsonDataFormat()
	{
		return jsonDataFormat;
	}

	public void setJsonDataFormat(JsonDataFormat jsonDataFormat)
	{
		this.jsonDataFormat = jsonDataFormat;
	}
}
