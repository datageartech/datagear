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

package org.datagear.analysis.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.httpresult.AbstractHttpResultHandler;
import org.datagear.analysis.support.httpresult.JsonHttpResultHandler;
import org.datagear.analysis.support.httpresult.TextHttpResultHandler;
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
	private static final long serialVersionUID = 1L;

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
	 * 请求内容类型：TEXT，对应的HTTP请求类型为：text/plain
	 */
	public static final String REQUEST_CONTENT_TYPE_TEXT = "TEXT";

	/**
	 * 请求内容类型：TEXT_XML，对应的HTTP请求类型为：text/xml
	 */
	public static final String REQUEST_CONTENT_TYPE_TEXT_XML = "TEXT_XML";

	/**
	 * 响应内容类型：JSON
	 */
	public static final String RESPONSE_CONTENT_TYPE_JSON = "JSON";

	/**
	 * 响应内容类型：文本
	 */
	public static final String RESPONSE_CONTENT_TYPE_TEXT = "TEXT";

	protected static final List<NameValuePair> NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON = new ArrayList<>(0);

	/** HTTP客户端 */
	private transient HttpClient httpClient;

	/** HTTP请求地址 */
	private String uri;

	/**
	 * 是否编码uri。
	 * <p>
	 * 当uri中包含中文时，通常需要进行编码。
	 * </p>
	 * <p>
	 * 注意：这个属性在{@code 4.7.0}版本之前是没有的，因此默认值应设为{@code false}，以兼容旧数据。
	 * </p>
	 * 
	 * @since 4.7.0
	 */
	private boolean encodeUri = false;

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

	/**
	 * 当{@code #responseContentType}是{@linkplain #RESPONSE_CONTENT_TYPE_JSON}时，响应数据的JSON路径
	 */
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

	public HttpDataSet(String id, String name, List<DataSetField> fields, HttpClient httpClient, String uri)
	{
		super(id, name, fields);
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

	public boolean isEncodeUri()
	{
		return encodeUri;
	}

	public void setEncodeUri(boolean encodeUri)
	{
		this.encodeUri = encodeUri;
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
	 * 目前支持参考{@code RESPONSE_CONTENT_TYPE_*}，{@linkplain #RESPONSE_CONTENT_TYPE_JSON}是默认值。
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
	protected TemplateResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveFields)
			throws DataSetException
	{
		String uri = null;
		String headerContent = null;
		String requestContent = null;

		try
		{
			uri = resolveTemplateUri(query);
			uri = encodeUriIfRequired(uri);
			ClassicHttpRequest request = createHttpRequest(uri);
			headerContent = setHttpHeaders(request, query);
			requestContent = setHttpEntity(request, query);

			AbstractHttpResultHandler resultHandler = buildHttpResultHandler(query, resolveFields);
			ResolvedDataSetResult result = this.httpClient.execute(request, resultHandler);

			return buildTemplateResolvedDataSetResult(result, uri, headerContent, requestContent);
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

	protected TemplateResolvedDataSetResult buildTemplateResolvedDataSetResult(ResolvedDataSetResult result, String uri,
			String headerContent, String requestContent) throws Throwable
	{
		return new TemplateResolvedDataSetResult(result.getResult(), result.getFields(),
				buildResolvedTemplate(uri, headerContent, requestContent));
	}

	protected AbstractHttpResultHandler buildHttpResultHandler(DataSetQuery query, boolean resolveFields)
			throws Exception
	{
		AbstractHttpResultHandler resultHandler;

		if (RESPONSE_CONTENT_TYPE_TEXT.equalsIgnoreCase(getResponseContentType()))
		{
			resultHandler = new TextHttpResultHandler(this, query, resolveFields);
		}
		else
		{
			resultHandler = new JsonHttpResultHandler(this, query, resolveFields);
		}

		return resultHandler;
	}

	/**
	 * 对URI进行编码，避免出现中文参数乱码问题。
	 * 
	 * @param uri
	 * @return
	 * @throws URISyntaxException
	 */
	protected String encodeUriIfRequired(String uri) throws URISyntaxException
	{
		if (!this.encodeUri)
			return uri;

		return new URI(uri).toASCIIString();
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

	protected String setHttpHeaders(ClassicHttpRequest request, DataSetQuery query) throws Throwable
	{
		if (StringUtil.isEmpty(this.headerContent))
			return "";

		String headerContent = resolveTemplateJson(this.headerContent, query);

		List<NameValuePair> headers = toNameValuePairs(headerContent);

		if (headers == NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON)
			throw new HeaderContentNotNameValueObjArrayJsonException(this.headerContent);

		for (NameValuePair header : headers)
			request.setHeader(header.getName(), header.getValue());

		return headerContent;
	}

	protected String setHttpEntity(ClassicHttpRequest request, DataSetQuery query) throws Throwable
	{
		String requestContent = this.requestContent;

		if (REQUEST_CONTENT_TYPE_FORM_URLENCODED.equals(this.requestContentType))
		{
			requestContent = resolveTemplateJson(this.requestContent, query);
			List<NameValuePair> params = toNameValuePairs(requestContent);

			if (params == NOT_NAME_VALUE_PAIR_OBJ_ARRAY_JSON)
				throw new RequestContentNotNameValueObjArrayJsonException(this.requestContent);

			request.setEntity(new UrlEncodedFormEntity(params, Charset.forName(this.requestContentCharset)));
		}
		else if (REQUEST_CONTENT_TYPE_JSON.equals(this.requestContentType))
		{
			requestContent = resolveTemplateJson(this.requestContent, query);
			ContentType contentType = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(),
					Charset.forName(this.requestContentCharset));
			StringEntity entity = new StringEntity(requestContent, contentType);
			request.setEntity(entity);
		}
		else if (REQUEST_CONTENT_TYPE_TEXT.equals(this.requestContentType))
		{
			requestContent = resolveTemplatePlain(this.requestContent, query);
			ContentType contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(),
					Charset.forName(this.requestContentCharset));
			StringEntity entity = new StringEntity(requestContent, contentType);
			request.setEntity(entity);
		}
		else if (REQUEST_CONTENT_TYPE_TEXT_XML.equals(this.requestContentType))
		{
			requestContent = resolveTemplateXml(this.requestContent, query);
			ContentType contentType = ContentType.create(ContentType.TEXT_XML.getMimeType(),
					Charset.forName(this.requestContentCharset));
			StringEntity entity = new StringEntity(requestContent, contentType);
			request.setEntity(entity);
		}
		else
		{
			requestContent = resolveTemplatePlain(this.requestContent, query);
			StringEntity entity = new StringEntity(requestContent);
			request.setEntity(entity);
		}

		return requestContent;
	}

	protected String resolveTemplateUri(DataSetQuery query) throws Throwable
	{
		return resolveTemplatePlain(this.uri, query);
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
}
