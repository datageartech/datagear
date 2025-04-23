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

package org.datagear.analysis.support.httpresult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.HttpDataSet;
import org.datagear.util.IOUtil;

/**
 * 编码为Base64的二进制HTTP响应结果处理器。
 * 
 * @author datagear@163.com
 *
 */
public class Base64HttpResultHandler extends AbstractRawHttpResultHandler
{
	public Base64HttpResultHandler()
	{
		super();
	}

	public Base64HttpResultHandler(HttpDataSet dataSet, DataSetQuery dataSetQuery, boolean resolveFields)
	{
		super(dataSet, dataSetQuery, resolveFields);
	}

	@Override
	protected Object readRawData(ClassicHttpResponse response) throws HttpException, IOException
	{
		InputStream in = null;

		try
		{
			in = response.getEntity().getContent();
			byte[] bytes = IOUtil.readBytes(in, false);
			return Base64.getEncoder().encodeToString(bytes);
		}
		finally
		{
			IOUtil.close(in);
		}
	}
}