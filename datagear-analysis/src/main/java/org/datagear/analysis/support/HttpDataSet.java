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

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractJsonDataSet.JsonDataSetResource;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP数据集。
 * <p>
 * 此类的{@linkplain #getUri()}、{@linkplain #getHeaderContent()}、{@linkplain #getRequestContent()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HttpDataSet extends AbstractResolvableDataSet
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(HttpDataSet.class);

	public static final String REQUEST_METHOD_GET = "GET";

	public static final String REQUEST_METHOD_POST = "POST";

	public static final String REQUEST_METHOD_PUT = "PUT";

	public static final String REQUEST_METHOD_PATCH = "PATCH";

	public static final String REQUEST_METHOD_DELETE = "DELETE";

	// 这些HTTP方法不适应于此
	// public static final String REQUEST_METHOD_HEAD = "HEAD";
	// public static final String REQUEST_METHOD_OPTIONS = "OPTIONS";
	// public static final String REQUEST_METHOD_TRACE = "TRACE";

	/**
	 * 请求内容类型：表单式的参数名/值类型，对应的HTTP请求类型为：application/x-www-form-urlencoded
	 */
	public static final String REQUEST_CONTENT_TYPE_FORM_URLENCODED = "FORM_URLENCODED";

	/**
	 * 请求内容类型：JSON，对应的HTTP请求类型为：application/json
	 */
	public static final String REQUEST_CONTENT_TYPE_JSON = "JSON";

	/**
	 * 响应内容类型：JSON，对应的HTTP响应类型为：application/json
	 */
	public static final String RESPONSE_CONTENT_TYPE_JSON = "JSON";

	protected static final List<NameValuePair> NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON = new ArrayList<>(0);

	/** HTTP客户端 */
	private HttpClient httpClient;

	/** HTTP请求地址 */
	private String uri;

	/** 请求头JSON文本 */
	private String headerContent = "";

	/** 请求方法 */
	private String requestMethod = REQUEST_METHOD_GET;

	/** 请求内容类型 */
	private String requestContentType = REQUEST_CONTENT_TYPE_FORM_URLENCODED;

	/** 请求内容编码 */
	private String requestContentCharset = IOUtil.CHARSET_UTF_8;

	/** 请求内容JSON文本 */
	private String requestContent = "";

	/** 响应类型 */
	private String responseContentType = RESPONSE_CONTENT_TYPE_JSON;

	/** 响应数据的JSON路径 */
	private String responseDataJsonPath = "";

	public HttpDataSet()
	{
		super();
	}

	public HttpDataSet(String id, String name, HttpClient httpClient, String uri)
	{
		super(id, name);
		this.httpClient = httpClient;
		this.uri = uri;
	}

	public HttpDataSet(String id, String name, List<DataSetProperty> properties, HttpClient httpClient, String uri)
	{
		super(id, name, properties);
		this.httpClient = httpClient;
		this.uri = uri;
	}

	public HttpClient getHttpClient()
	{
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	public String getUri()
	{
		return uri;
	}

	/**
	 * 设置请求地址。
	 * <p>
	 * 请求地址支持<code>Freemarker</code>模板语言。
	 * </p>
	 * 
	 * @param uri
	 */
	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getHeaderContent()
	{
		return headerContent;
	}

	/**
	 * 设置请求头JSON文本，格式应为：
	 * <p>
	 * <code>
	 * <pre>
	 * [
	 *   {name: "...", value: "..."},
	 *   {name: "...", value: "..."},
	 *   ...
	 * ]
	 * </pre>
	 * </code>
	 * </p>
	 * <p>
	 * 请求头JSON文本支持<code>Freemarker</code>模板语言。
	 * </p>
	 * 
	 * @param headerContent
	 */
	public void setHeaderContent(String headerContent)
	{
		this.headerContent = headerContent;
	}

	public String getRequestMethod()
	{
		return requestMethod;
	}

	/**
	 * 设置HTTP方法，参考{@code REQUEST_METHOD_*}常量。
	 * 
	 * @param requestMethod
	 */
	public void setRequestMethod(String requestMethod)
	{
		this.requestMethod = requestMethod;
	}

	public String getRequestContentType()
	{
		return requestContentType;
	}

	/**
	 * 设置请求内容类型，允许的值为：
	 * <p>
	 * {@linkplain #REQUEST_CONTENT_TYPE_FORM_URLENCODED}、{@linkplain #REQUEST_CONTENT_TYPE_JSON}。
	 * </p>
	 * 
	 * @param requestContentType
	 */
	public void setRequestContentType(String requestContentType)
	{
		this.requestContentType = requestContentType;
	}

	public String getRequestContentCharset()
	{
		return requestContentCharset;
	}

	/**
	 * 设置请求内容编码。
	 * <p>
	 * 默认请求内容编码为{@code UTF-8}。
	 * </p>
	 * 
	 * @param requestContentCharset
	 */
	public void setRequestContentCharset(String requestContentCharset)
	{
		this.requestContentCharset = requestContentCharset;
	}

	public String getRequestContent()
	{
		return requestContent;
	}

	/**
	 * 设置请求内容JSON文本，为{@code null}或{@code ""}表示无请求内容。
	 * <p>
	 * 当{@linkplain #getRequestContentType()}为{@linkplain #REQUEST_CONTENT_TYPE_FORM_URLENCODED}时，请求内容JSON文本格式应为：
	 * </p>
	 * <code>
	 * <pre>
	 * [
	 *   {name: "...", value: "..."},
	 *   {name: "...", value: "..."},
	 *   ...
	 * ]
	 * </pre>
	 * </code>
	 * <p>
	 * 其中，{@code name}表示请求参数名，{@code value}表示请求参数值。
	 * </p>
	 * <p>
	 * 当{@linkplain #getRequestContentType()}为{@linkplain #REQUEST_CONTENT_TYPE_JSON}时，请求内容JSON文本没有特殊格式要求。
	 * </p>
	 * <p>
	 * 请求内容JSON文本支持<code>Freemarker</code>模板语言。
	 * </p>
	 * 
	 * @param requestContent
	 */
	public void setRequestContent(String requestContent)
	{
		this.requestContent = requestContent;
	}

	public String getResponseContentType()
	{
		return responseContentType;
	}

	/**
	 * 设置相应类型。
	 * <p>
	 * 目前仅支持{@linkplain #RESPONSE_CONTENT_TYPE_JSON}，且是默认值。
	 * </p>
	 * 
	 * @param responseContentType
	 */
	public void setResponseContentType(String responseContentType)
	{
		this.responseContentType = responseContentType;
	}

	public String getResponseDataJsonPath()
	{
		return responseDataJsonPath;
	}

	/**
	 * 设置响应数据的JSON路径。
	 * <p>
	 * 当希望返回的是响应原始JSON数据的指定JSON路径值时，可以设置此项。
	 * </p>
	 * <p>
	 * 具体格式参考{@linkplain AbstractJsonDataSet#setDataJsonPath(String)}。
	 * </p>
	 * <p>
	 * 默认无数据路径，将直接返回响应原始JSON数据。
	 * </p>
	 * 
	 * @param responseDataJsonPath
	 */
	public void setResponseDataJsonPath(String responseDataJsonPath)
	{
		this.responseDataJsonPath = responseDataJsonPath;
	}

	@Override
	public TemplateResolvedDataSetResult resolve(DataSetQuery query)
			throws DataSetException
	{
		return (TemplateResolvedDataSetResult) super.resolve(query);
	}

	@Override
	protected TemplateResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		String uri = null;
		String headerContent = null;
		String requestContent = null;

		try
		{
			uri = resolveTemplateUri(query);
			headerContent = resolveTemplateHeaderContent(query);
			requestContent = resolveTemplateRequestContent(query);

			ClassicHttpRequest request = createHttpRequest(uri);

			setHttpHeaders(request, headerContent);
			setHttpEntity(request, requestContent);

			JsonResponseHandler responseHandler = new JsonResponseHandler(query, properties, resolveProperties,
					getResponseDataJsonPath());

			ResolvedDataSetResult result = this.httpClient.execute(request, responseHandler);

			return new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
					buildResolvedTemplate(uri, headerContent, requestContent));
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t, buildResolvedTemplate(uri, headerContent, requestContent));
		}
	}

	protected String buildResolvedTemplate(String uri, String headerContent, String requestContent)
	{
		StringBuilder sb = new StringBuilder();

		if (!StringUtil.isEmpty(uri))
		{
			if (sb.length() > 0)
				sb.append(
						System.lineSeparator() + "-----------------------------------------" + System.lineSeparator());

			sb.append("URI:" + System.lineSeparator() + uri);
		}

		if (!StringUtil.isEmpty(headerContent))
		{
			if (sb.length() > 0)
				sb.append(
						System.lineSeparator() + "-----------------------------------------" + System.lineSeparator());

			sb.append("Request headers:" + System.lineSeparator() + headerContent);
		}

		if (!StringUtil.isEmpty(requestContent))
		{
			if (sb.length() > 0)
				sb.append(
						System.lineSeparator() + "-----------------------------------------" + System.lineSeparator());

			sb.append("Request content:" + System.lineSeparator() + requestContent);
		}

		return sb.toString();
	}

	protected void setHttpHeaders(ClassicHttpRequest request, String headerContent) throws Throwable
	{
		if (StringUtil.isEmpty(headerContent))
			return;

		List<NameValuePair> headers = toNameValuePairs(headerContent);

		if (headers == NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON)
			throw new HeaderContentNotNameValueObjArrayJsonException(headerContent);

		for (NameValuePair header : headers)
			request.setHeader(header.getName(), header.getValue());
	}

	protected void setHttpEntity(ClassicHttpRequest request, String requestContent) throws Throwable
	{
		if (REQUEST_CONTENT_TYPE_FORM_URLENCODED.equals(this.requestContentType))
		{
			List<NameValuePair> params = toNameValuePairs(requestContent);

			if (params == NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON)
				throw new RequestContentNotNameValueObjArrayJsonException(requestContent);

			request.setEntity(new UrlEncodedFormEntity(params, Charset.forName(this.requestContentCharset)));
		}
		else if (REQUEST_CONTENT_TYPE_JSON.equals(this.requestContentType))
		{
			ContentType contentType = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(),
					Charset.forName(this.requestContentCharset));
			StringEntity entity = new StringEntity(requestContent, contentType);
			request.setEntity(entity);
		}
		else
			throw new DataSetException("Request content type [" + this.requestContentType + "] is not supported");
	}

	protected String resolveTemplateUri(DataSetQuery query) throws Throwable
	{
		return resolveTextAsGeneralTemplate(this.uri, query);
	}

	protected String resolveTemplateHeaderContent(DataSetQuery query) throws Throwable
	{
		return resolveJsonAsTemplate(this.headerContent, query);
	}

	protected String resolveTemplateRequestContent(DataSetQuery query) throws Throwable
	{
		return resolveJsonAsTemplate(this.requestContent, query);
	}

	/**
	 * 将指定JSON文本作为模板解析。
	 * 
	 * @param json
	 * @param query
	 * @return
	 */
	protected String resolveJsonAsTemplate(String json, DataSetQuery query)
	{
		return resolveTextAsTemplate(AbstractJsonDataSet.JSON_TEMPLATE_RESOLVER, json, query);
	}

	protected ClassicHttpRequest createHttpRequest(String uri) throws Throwable
	{
		if (REQUEST_METHOD_GET.equals(this.requestMethod) || StringUtil.isEmpty(this.requestMethod))
			return new HttpGet(uri);
		else if (REQUEST_METHOD_POST.equals(this.requestMethod))
			return new HttpPost(uri);
		else if (REQUEST_METHOD_PUT.equals(this.requestMethod))
			return new HttpPut(uri);
		else if (REQUEST_METHOD_PATCH.equals(this.requestMethod))
			return new HttpPatch(uri);
		else if (REQUEST_METHOD_DELETE.equals(this.requestMethod))
			return new HttpDelete(uri);
		// else if (REQUEST_METHOD_HEAD.equals(this.httpMethod))
		// return new HttpHead(uri);
		// else if (REQUEST_METHOD_OPTIONS.equals(this.httpMethod))
		// return new HttpOptions(uri);
		// else if (REQUEST_METHOD_TRACE.equals(this.httpMethod))
		// return new HttpTrace(uri);
		else
			throw new DataSetException("HTTP method [" + this.requestMethod + "] is not supported");
	}

	/**
	 * 将指定JSON字符串转换为名/值列表。
	 * 
	 * @param nameValueObjJsonArray
	 *            允许为{@code null}、{@code ""}
	 * @return 空列表表示无名/值，返回{@code #NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON}表示{@code nameValueObjJsonArray}格式不合法
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<NameValuePair> toNameValuePairs(String nameValueObjJsonArray) throws Throwable
	{
		if (StringUtil.isEmpty(nameValueObjJsonArray))
			return Collections.EMPTY_LIST;

		Object jsonObj = getObjectMapperNonStardand().readValue(nameValueObjJsonArray, Object.class);

		if (jsonObj == null)
			return Collections.EMPTY_LIST;

		if (!(jsonObj instanceof Collection<?>))
			return NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON;

		Collection<?> collection = (Collection<?>) jsonObj;

		List<NameValuePair> nameValuePairs = new ArrayList<>(collection.size());

		for (Object ele : collection)
		{
			String name = null;
			String value = null;

			if (ele instanceof Map<?, ?>)
			{
				Map<String, ?> eleMap = (Map<String, ?>) ele;
				Object nameVal = eleMap.get("name");
				Object valueVal = eleMap.get("value");

				if (nameVal instanceof String)
				{
					name = (String) nameVal;
					if (valueVal != null)
						value = (valueVal instanceof String ? (String) valueVal : valueVal.toString());
				}
			}

			if (name == null)
				return NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON;

			nameValuePairs.add(new BasicNameValuePair(name, value));
		}

		return nameValuePairs;
	}

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}

	protected static class JsonResponseHandler implements HttpClientResponseHandler<ResolvedDataSetResult>
	{
		private DataSetQuery dataSetQuery;

		private List<DataSetProperty> properties;

		private boolean resolveProperties;

		private String responseDataJsonPath;

		public JsonResponseHandler()
		{
			super();
		}

		public JsonResponseHandler(DataSetQuery dataSetQuery, List<DataSetProperty> properties,
				boolean resolveProperties, String responseDataJsonPath)
		{
			super();
			this.dataSetQuery = dataSetQuery;
			this.properties = (properties == null ? Collections.emptyList() : properties);
			this.resolveProperties = resolveProperties;
			this.responseDataJsonPath = responseDataJsonPath;
		}

		public DataSetQuery getDataSetQuery()
		{
			return dataSetQuery;
		}

		public void setDataSetQuery(DataSetQuery dataSetQuery)
		{
			this.dataSetQuery = dataSetQuery;
		}

		public List<DataSetProperty> getProperties()
		{
			return properties;
		}

		public void setProperties(List<DataSetProperty> properties)
		{
			this.properties = properties;
		}

		public boolean isResolveProperties()
		{
			return resolveProperties;
		}

		public void setResolveProperties(boolean resolveProperties)
		{
			this.resolveProperties = resolveProperties;
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
		public ResolvedDataSetResult handleResponse(ClassicHttpResponse response) throws HttpException, IOException
		{
			int code = response.getCode();
			HttpEntity entity = response.getEntity();

			if (code < 200 || code >= 300)
				throw new HttpResponseException(code, response.getReasonPhrase());

			Reader reader = null;

			if (entity == null)
				reader = IOUtil.getReader("");
			else
			{
				Charset contentCharset = resolveCharset(entity, ContentType.APPLICATION_JSON.getCharset());
				reader = IOUtil.getReader(entity.getContent(), contentCharset);
			}

			if (this.resolveProperties)
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(this.properties, reader,
						this.responseDataJsonPath);
				return jsonDataSet.resolve(this.dataSetQuery);
			}
			else
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(this.properties, reader,
						this.responseDataJsonPath);

				DataSetResult result = jsonDataSet.getResult(this.dataSetQuery);
				return new ResolvedDataSetResult(result, this.properties);
			}
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
					LOGGER.warn("Default charset [" + defaultCharset + "] will be used because parse error", t);

					contentCharset = defaultCharset;
				}
			}

			return (contentCharset != null ? contentCharset : defaultCharset);
		}
	}

	protected static class HttpResponseJsonDataSet extends AbstractJsonDataSet<HttpResponseJsonDataSetResource>
	{
		private Reader responseJsonReader;

		public HttpResponseJsonDataSet(Reader responseJsonReader, String responseDataJsonPath)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName());
			this.responseJsonReader = responseJsonReader;
			super.setDataJsonPath(responseDataJsonPath);
		}

		public HttpResponseJsonDataSet(List<DataSetProperty> properties, Reader responseJsonReader,
				String responseDataJsonPath)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName(), properties);
			this.responseJsonReader = responseJsonReader;
			super.setDataJsonPath(responseDataJsonPath);
		}

		@Override
		protected HttpResponseJsonDataSetResource getResource(DataSetQuery query, List<DataSetProperty> properties,
				boolean resolveProperties) throws Throwable
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
	}
}
