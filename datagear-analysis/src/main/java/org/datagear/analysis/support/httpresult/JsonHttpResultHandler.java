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
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractJsonDataSet;
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
	private String responseDataJsonPath;

	public JsonHttpResultHandler()
	{
		super();
	}

	public JsonHttpResultHandler(DataSetQuery dataSetQuery, List<DataSetField> fields,
			boolean resolveFields, String responseDataJsonPath)
	{
		super(dataSetQuery, fields, resolveFields);
		this.responseDataJsonPath = responseDataJsonPath;
	}

	public String getResponseDataJsonPath()
	{
		return responseDataJsonPath;
	}

	public void setResponseDataJsonPath(String responseDataJsonPath)
	{
		this.responseDataJsonPath = responseDataJsonPath;
	}

	@Override
	protected ResolvedDataSetResult doHandleResponse(ClassicHttpResponse response) throws HttpException, IOException
	{
		HttpEntity entity = response.getEntity();
		Reader reader = getReader(response, entity);

		try
		{
			if (this.isResolveFields())
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(this.getFields(), reader,
						this.responseDataJsonPath);
				return jsonDataSet.resolve(this.getDataSetQuery());
			}
			else
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(this.getFields(), reader,
						this.responseDataJsonPath);

				DataSetResult result = jsonDataSet.getResult(this.getDataSetQuery());
				return new ResolvedDataSetResult(result, this.getFields());
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

		public HttpResponseJsonDataSet(Reader responseJsonReader, String responseDataJsonPath)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName());
			this.responseJsonReader = responseJsonReader;
			super.setDataJsonPath(responseDataJsonPath);
		}

		public HttpResponseJsonDataSet(List<DataSetField> fields, Reader responseJsonReader,
				String responseDataJsonPath)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName(), fields);
			this.responseJsonReader = responseJsonReader;
			super.setDataJsonPath(responseDataJsonPath);
		}

		@Override
		protected HttpResponseJsonDataSetResource getResource(DataSetQuery query) throws Throwable
		{
			return new HttpResponseJsonDataSetResource("", getDataJsonPath(), this.responseJsonReader);
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

		public HttpResponseJsonDataSetResource(String resolvedTemplate, String dataJsonPath, Reader jsonReader)
		{
			super(resolvedTemplate, dataJsonPath);
			this.jsonReader = jsonReader;
		}

		public Reader getJsonReader()
		{
			return jsonReader;
		}

		@Override
		public boolean isIdempotent()
		{
			return false;
		}

		@Override
		public Reader getReader() throws Throwable
		{
			return this.jsonReader;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [dataJsonPath=" + getDataJsonPath() + ", resolvedTemplate="
					+ getResolvedTemplate() + "]";
		}
	}
}