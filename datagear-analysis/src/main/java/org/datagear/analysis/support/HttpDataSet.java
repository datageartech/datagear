/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
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
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
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
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
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

	public static final String HTTP_METHOD_GET = "GET";

	public static final String HTTP_METHOD_POST = "POST";

	public static final String HTTP_METHOD_PUT = "PUT";

	public static final String HTTP_METHOD_HEAD = "HEAD";

	public static final String HTTP_METHOD_PATCH = "PATCH";

	public static final String HTTP_METHOD_DELETE = "DELETE";

	public static final String HTTP_METHOD_OPTIONS = "OPTIONS";

	public static final String HTTP_METHOD_TRACE = "TRACE";

	/**
	 * 内容类型：表单式的参数名/值类型
	 */
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	/**
	 * 内容类型：JSON
	 */
	public static final String CONTENT_TYPE_JSON = "application/json";

	/** HTTP客户端 */
	private HttpClient httpClient;

	/** HTTP请求地址 */
	private String uri;

	/** 请求方法 */
	private String httpMethod = HTTP_METHOD_GET;

	/** 请求头JSON文本 */
	private String headerContent = "";

	/** 请求内容类型 */
	private String requestContentType = CONTENT_TYPE_FORM;

	/** 请求内容编码 */
	private String requestContentCharset = IOUtil.CHARSET_UTF_8;

	/** 请求内容JSON文本 */
	private String requestContent = "";

	/** 响应类型 */
	private String responseContentType = CONTENT_TYPE_JSON;

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

	public String getHttpMethod()
	{
		return httpMethod;
	}

	/**
	 * 设置HTTP方法，参考{@code HTTP_METHOD_*}常量。
	 * 
	 * @param httpMethod
	 */
	public void setHttpMethod(String httpMethod)
	{
		this.httpMethod = httpMethod;
	}

	public String getHeaderContent()
	{
		return headerContent;
	}

	/**
	 * 设置请求头JSON文本，格式为： <code>
	 * <pre>
	 * [
	 *   {name: "...", value: "..."},
	 *   {name: "...", value: "..."},
	 *   ...
	 * ]
	 * </pre>
	 * </code>
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

	public String getRequestContentType()
	{
		return requestContentType;
	}

	/**
	 * 设置请求内容类型，参考{@code CONTENT_TYPE_*}。
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
	 * 当{@linkplain #getRequestContentType()}为{@linkplain #CONTENT_TYPE_FORM}时，请求内容JSON文本格式应为：
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
	 * 当{@linkplain #getRequestContentType()}为{@linkplain #CONTENT_TYPE_JSON}时，请求内容JSON文本没有特殊格式要求。
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
	 * 目前相应类型仅支持{@linkplain #CONTENT_TYPE_JSON}。
	 * </p>
	 * 
	 * @param responseContentType
	 */
	public void setResponseContentType(String responseContentType)
	{
		this.responseContentType = responseContentType;
	}

	@Override
	public TemplateResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException
	{
		return resolveResult(paramValues, null);
	}

	@Override
	protected TemplateResolvedDataSetResult resolveResult(Map<String, ?> paramValues, List<DataSetProperty> properties)
			throws DataSetException
	{
		CloseableHttpResponse response = null;

		try
		{
			String uri = resolveTemplateUri(paramValues);
			String headerContent = resolveTemplateHeaderContent(paramValues);
			String requestContent = resolveTemplateRequestContent(paramValues);

			ClassicHttpRequest request = createHttpRequest(uri);

			setHttpHeaders(request, headerContent);
			setHttpEntity(request, requestContent);

			JsonResponseHandler responseHandler = new JsonResponseHandler();
			responseHandler.setProperties(properties);

			ResolvedDataSetResult result = this.httpClient.execute(request, responseHandler);

			String templateResult = "URI:" + System.lineSeparator() + uri //
					+ System.lineSeparator() + "-----------------------------------------" + System.lineSeparator() //
					+ "Headers:" + System.lineSeparator() + headerContent //
					+ System.lineSeparator() + "-----------------------------------------" + System.lineSeparator() //
					+ "Request content:" + System.lineSeparator() + requestContent;

			return new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(), templateResult);
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}
		finally
		{
			IOUtil.close(response);
		}
	}

	protected void setHttpHeaders(ClassicHttpRequest request, String headerContent) throws Throwable
	{
		if (StringUtil.isEmpty(headerContent))
			return;

		List<NameValuePair> headers = toNameValuePairs(headerContent);

		for (NameValuePair header : headers)
			request.setHeader(header.getName(), header.getValue());
	}

	protected void setHttpEntity(ClassicHttpRequest request, String requestContent) throws Throwable
	{
		if (CONTENT_TYPE_FORM.equals(this.requestContentType))
		{
			List<NameValuePair> params = toNameValuePairs(requestContent);
			request.setEntity(new UrlEncodedFormEntity(params, Charset.forName(this.requestContentCharset)));
		}
		else if (CONTENT_TYPE_JSON.equals(this.requestContentType))
		{
			StringEntity entity = new StringEntity(requestContent, ContentType.APPLICATION_JSON);
			request.setEntity(entity);
		}
		else
			throw new DataSetException("HTTP request content-type [" + this.requestContentType + "] is not supported");
	}

	protected String resolveTemplateUri(Map<String, ?> paramValues) throws Throwable
	{
		return resolveAsFmkTemplate(this.uri, paramValues);
	}

	protected String resolveTemplateHeaderContent(Map<String, ?> paramValues) throws Throwable
	{
		return resolveAsFmkTemplate(this.headerContent, paramValues);
	}

	protected String resolveTemplateRequestContent(Map<String, ?> paramValues) throws Throwable
	{
		return resolveAsFmkTemplate(this.requestContent, paramValues);
	}

	protected ClassicHttpRequest createHttpRequest(String uri) throws Throwable
	{
		if (HTTP_METHOD_GET.equals(this.httpMethod))
			return new HttpGet(uri);
		else if (HTTP_METHOD_POST.equals(this.httpMethod))
			return new HttpPost(uri);
		else if (HTTP_METHOD_PUT.equals(this.httpMethod))
			return new HttpPut(uri);
		else if (HTTP_METHOD_HEAD.equals(this.httpMethod))
			return new HttpHead(uri);
		else if (HTTP_METHOD_PATCH.equals(this.httpMethod))
			return new HttpPatch(uri);
		else if (HTTP_METHOD_DELETE.equals(this.httpMethod))
			return new HttpDelete(uri);
		else if (HTTP_METHOD_OPTIONS.equals(this.httpMethod))
			return new HttpOptions(uri);
		else if (HTTP_METHOD_TRACE.equals(this.httpMethod))
			return new HttpTrace(uri);
		else
			throw new DataSetException("HTTP method [" + this.httpMethod + "] is not supported");
	}

	/**
	 * 将指定JSON字符串转换为名/值列表。
	 * 
	 * @param jsonArrayContent
	 *            可能为{@code null}、{@code ""}
	 * @return 空列表表示无名/值
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected List<NameValuePair> toNameValuePairs(String jsonArrayContent) throws Throwable
	{
		if (StringUtil.isEmpty(jsonArrayContent))
			return Collections.EMPTY_LIST;

		Object jsonObj = getObjectMapperNonStardand().readValue(jsonArrayContent, Object.class);

		if (jsonObj == null)
			return Collections.EMPTY_LIST;

		if (!(jsonObj instanceof Collection<?>))
			throw new DataSetException("The content must be JSON array");

		Collection<?> collection = (Collection<?>) jsonObj;

		List<NameValuePair> nameValuePairs = new ArrayList<>(collection.size());

		int idx = 0;
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
				throw new DataSetException(
						"The content " + idx + "-th element must be JSON object : {name: \"...\", value: \"...\"}");

			nameValuePairs.add(new BasicNameValuePair(name, value));

			idx++;
		}

		return nameValuePairs;
	}

	protected ObjectMapper getObjectMapperNonStardand()
	{
		return JsonSupport.getObjectMapperNonStardand();
	}

	/**
	 * HTTP单个请求头。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HttpHeader implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 名称 */
		private String name;

		/** 值 */
		private String value;

		public HttpHeader()
		{
			super();
		}

		public HttpHeader(String name, String value)
		{
			super();
			this.name = name;
			this.value = value;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HttpHeader other = (HttpHeader) obj;
			if (name == null)
			{
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			if (value == null)
			{
				if (other.value != null)
					return false;
			}
			else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", value=" + value + "]";
		}
	}

	protected static class JsonResponseHandler implements HttpClientResponseHandler<ResolvedDataSetResult>
	{
		private List<DataSetProperty> properties;

		public JsonResponseHandler()
		{
			super();
		}

		public List<DataSetProperty> getProperties()
		{
			return properties;
		}

		/**
		 * 设置数据集属性。
		 * 
		 * @param properties
		 *            如果为{@code null}或空，则执行解析
		 */
		public void setProperties(List<DataSetProperty> properties)
		{
			this.properties = properties;
		}

		@SuppressWarnings("unchecked")
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

			if (this.properties == null || this.properties.isEmpty())
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(reader);
				return jsonDataSet.resolve(Collections.EMPTY_MAP);
			}
			else
			{
				HttpResponseJsonDataSet jsonDataSet = new HttpResponseJsonDataSet(this.properties, reader);
				DataSetResult result = jsonDataSet.getResult(Collections.EMPTY_MAP);
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

	protected static class HttpResponseJsonDataSet extends AbstractJsonDataSet
	{
		private Reader responseJsonReader;

		public HttpResponseJsonDataSet(Reader responseJsonReader)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName());
			this.responseJsonReader = responseJsonReader;
		}

		public HttpResponseJsonDataSet(List<DataSetProperty> properties, Reader responseJsonReader)
		{
			super(HttpResponseJsonDataSet.class.getName(), HttpResponseJsonDataSet.class.getName(), properties);
			this.responseJsonReader = responseJsonReader;
		}

		@Override
		protected TemplateResolvedSource<Reader> getJsonReader(Map<String, ?> paramValues) throws Throwable
		{
			return new TemplateResolvedSource<>(this.responseJsonReader);
		}
	}
}
