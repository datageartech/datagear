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
import java.nio.charset.Charset;
import java.util.List;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象HTTP响应结果处理器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractHttpResultHandler implements HttpClientResponseHandler<ResolvedDataSetResult>
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpResultHandler.class);

	private DataSetQuery dataSetQuery;

	private List<DataSetField> fields;

	private boolean resolveFields;

	public AbstractHttpResultHandler()
	{
		super();
	}

	public AbstractHttpResultHandler(DataSetQuery dataSetQuery, List<DataSetField> fields, boolean resolveFields)
	{
		super();
		this.dataSetQuery = dataSetQuery;
		this.fields = fields;
		this.resolveFields = resolveFields;
	}

	public DataSetQuery getDataSetQuery()
	{
		return dataSetQuery;
	}

	public void setDataSetQuery(DataSetQuery dataSetQuery)
	{
		this.dataSetQuery = dataSetQuery;
	}

	public List<DataSetField> getFields()
	{
		return fields;
	}

	public void setFields(List<DataSetField> fields)
	{
		this.fields = fields;
	}

	public boolean isResolveFields()
	{
		return resolveFields;
	}

	public void setResolveFields(boolean resolveFields)
	{
		this.resolveFields = resolveFields;
	}

	@Override
	public ResolvedDataSetResult handleResponse(ClassicHttpResponse response) throws HttpException, IOException
	{
		int code = response.getCode();

		if (code < 200 || code >= 300)
			throw new HttpResponseException(code, response.getReasonPhrase());

		return this.doHandleResponse(response);
	}

	protected abstract ResolvedDataSetResult doHandleResponse(ClassicHttpResponse response)
			throws HttpException, IOException;

	protected Reader getReader(ClassicHttpResponse response, HttpEntity entity) throws HttpException, IOException
	{
		Reader reader = null;

		if (entity == null)
			reader = IOUtil.getReader("");
		else
		{
			Charset contentCharset = resolveCharset(entity, ContentType.APPLICATION_JSON.getCharset());
			reader = IOUtil.getReader(entity.getContent(), contentCharset);
		}

		return reader;
	}

	protected Charset resolveCharset(HttpEntity entity, Charset defaultCharset)
	{
		Charset contentCharset = null;

		String contentTypeStr = entity.getContentType();

		if (!StringUtil.isEmpty(contentTypeStr))
		{
			try
			{
				ContentType contentType = ContentType.parse(contentTypeStr);
				contentCharset = contentType.getCharset();
			}
			catch (Throwable t)
			{
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Default charset [" + defaultCharset + "] will be used because parse error", t);

				contentCharset = defaultCharset;
			}
		}

		return (contentCharset != null ? contentCharset : defaultCharset);
	}
}