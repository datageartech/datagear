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
import java.io.Reader;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractJsonDataSet;
import org.datagear.analysis.support.HttpDataSet;
import org.datagear.analysis.support.datasetres.JsonDataSetResource;
import org.datagear.util.IOUtil;

/**
 * JSON HTTP响应结果处理器。
 * 
 * @author datagear@163.com
 *
 */
public class JsonHttpResultHandler extends AbstractHttpResultHandler
{
	public JsonHttpResultHandler()
	{
		super();
	}

	public JsonHttpResultHandler(HttpDataSet dataSet, DataSetQuery dataSetQuery, boolean resolveFields)
	{
		super(dataSet, dataSetQuery, resolveFields);
	}

	@Override
	protected ResolvedDataSetResult doHandleResponse(ClassicHttpResponse response) throws HttpException, IOException
	{
		HttpEntity entity = response.getEntity();
		HttpDataSet dataSet = getDataSet();
		Reader reader = null;

		try
		{
			reader = getReader(response, entity);
			HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(dataSet, reader);

			if (this.isResolveFields())
			{
				return jsonDataSet.resolve(this.getDataSetQuery());
			}
			else
			{
				DataSetResult result = jsonDataSet.getResult(this.getDataSetQuery());
				return new ResolvedDataSetResult(result, dataSet.getFields());
			}
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	protected static class HttpResponseJsonDataSet extends AbstractJsonDataSet<HttpResponseJsonDataSetResource>
	{
		private static final long serialVersionUID = 1L;

		private transient Reader responseJsonReader;

		public HttpResponseJsonDataSet(HttpDataSet dataSet, Reader responseJsonReader)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName(),
					dataSet.getFields());
			setMutableModel(dataSet.isMutableModel());
			setParams(dataSet.getParams());
			setDataFormat(dataSet.getDataFormat());
			setDataJsonPath(dataSet.getResponseDataJsonPath());
			setAdditionDataProps(dataSet.getResponseAdditionDataProps());
			this.responseJsonReader = responseJsonReader;
		}

		@Override
		protected HttpResponseJsonDataSetResource getResource(DataSetQuery query) throws Throwable
		{
			return new HttpResponseJsonDataSetResource("", getDataJsonPath(), getAdditionDataProps(),
					this.responseJsonReader);
		}
	}

	protected static class HttpResponseJsonDataSetResource extends JsonDataSetResource
	{
		private static final long serialVersionUID = 1L;

		private transient Reader jsonReader;

		public HttpResponseJsonDataSetResource()
		{
			super();
		}

		public HttpResponseJsonDataSetResource(String resolvedTemplate, String dataJsonPath, String additionDataProps,
				Reader jsonReader)
		{
			super(resolvedTemplate, dataJsonPath, additionDataProps);
			this.jsonReader = jsonReader;
		}

		public Reader getJsonReader()
		{
			return jsonReader;
		}

		@Override
		public boolean isIdempotent()
		{
			// 这里应始终返回false
			return false;
		}

		@Override
		public Reader getReader() throws Throwable
		{
			return this.jsonReader;
		}
	}
}