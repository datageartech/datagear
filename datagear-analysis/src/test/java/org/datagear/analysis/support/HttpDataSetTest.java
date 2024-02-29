/*
 * Copyright 2018-2024 datagear.tech
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.util.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@linkplain HttpDataSet}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class HttpDataSetTest
{
	protected static final int PORT = 50402;

	protected static final String SERVER = "http://localhost:" + PORT;

	protected static final String PARAM_NAME_0 = "param0";

	protected static final String PARAM_NAME_1 = "param1";

	protected static HttpServer server;

	protected static CloseableHttpClient httpClient;

	@BeforeClass
	public static void initTestHttpServer() throws Throwable
	{
		server = ServerBootstrap.bootstrap().setListenerPort(PORT)
				//
				.register("/testSimple", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						StringEntity responseEntity = new StringEntity(
								"[{name: 'aaa', value: 11}, {name: '名称b', value: 22}]", ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testParam", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						Map<String, String> params = parseRequestParams(request);
						String p0 = params.get(PARAM_NAME_0);
						String p1 = params.get(PARAM_NAME_1);

						Map<String, String> re0 = new HashMap<String, String>();
						re0.put("name", PARAM_NAME_0);
						re0.put("value", p0);

						Map<String, String> re1 = new HashMap<String, String>();
						re1.put("name", PARAM_NAME_1);
						re1.put("value", p1);

						StringEntity responseEntity = new StringEntity(toJsonString(Arrays.asList(re0, re1)),
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testJson", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						String reqJson = getRequestStringContent(request);

						StringEntity responseEntity = new StringEntity(reqJson, ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testText", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{

						String reqStr = getRequestStringContent(request);
						Map<String, String> responseJson = new HashMap<String, String>();
						responseJson.put("value", reqStr);
						responseJson.put("contentType", request.getHeader("content-type").getValue());
						StringEntity responseEntity = new StringEntity(toJsonString(Arrays.asList(responseJson)),
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testXml", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						String reqStr = getRequestStringContent(request);
						Map<String, String> responseJson = new HashMap<String, String>();
						responseJson.put("value", reqStr);
						responseJson.put("contentType", request.getHeader("content-type").getValue());
						StringEntity responseEntity = new StringEntity(toJsonString(Arrays.asList(responseJson)),
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testHeader", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						Header h0 = request.getHeader(PARAM_NAME_0);
						Header h1 = request.getHeader(PARAM_NAME_1);

						StringEntity responseEntity = new StringEntity(
								"[{name: '" + PARAM_NAME_0 + "', value: '" + h0.getValue() + "'}, {name: '"
										+ PARAM_NAME_1 + "', value: '" + h1.getValue() + "'}]",
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.register("/testResponseJsonPath", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						StringEntity responseEntity = new StringEntity(
								"{ path0: { path1: [ { path2: [{name: 'aaa', value: 11}, {name: '名称b', value: 22}] } ] } }",
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				//
				.register("/testGetChineseParam", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						URI uri = null;

						try
						{
							uri = request.getUri();
						}
						catch (Exception e)
						{
							throw new IOException(e);
						}

						String q = uri.getQuery();

						StringEntity responseEntity = new StringEntity(
								"{ q : '" + q + "' }",
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				})
				//
				.create();

		server.start();

		httpClient = HttpClients.createDefault();
	}

	@AfterClass
	public static void closeHttpServer() throws Throwable
	{
		server.close();
		httpClient.close();
	}

	@Test
	public void resolveTest_onlyUri() throws Throwable
	{
		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testSimple?param=${param}");

		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", "pv");

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("param=pv"));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.NUMBER, property.getType());
			}
		}

		{
			assertEquals(2, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals("aaa", row.get("name"));
				assertEquals(11, ((Number) row.get("value")).intValue());
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals("名称b", row.get("name"));
				assertEquals(22, ((Number) row.get("value")).intValue());
			}
		}
	}

	@Test
	public void resolveTest_setRequestMethod_GET() throws Throwable
	{
		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testSimple");

		dataSet.setRequestMethod(HttpDataSet.REQUEST_METHOD_GET);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		assertEquals(2, properties.size());
		assertEquals(2, data.size());
	}

	@Test
	public void resolveTest_setRequestMethod_POST() throws Throwable
	{
		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testSimple");

		dataSet.setRequestMethod(HttpDataSet.REQUEST_METHOD_POST);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		assertEquals(2, properties.size());
		assertEquals(2, data.size());
	}

	@Test
	public void resolveTest_REQUEST_CONTENT_TYPE_FORM_URLENCODED() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "参数值1:\"---\\---\r---\n";
		String pv1JsonStr = "参数值1:\\\"---\\\\---\\r---\\n";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testParam");
		dataSet.setRequestContent("[ { name: \"" + PARAM_NAME_0 + "\", value: \"" + pv0 + "\" }, { name: \""
				+ PARAM_NAME_1 + "\", value: \"${param}\" } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("value: \"" + pv1JsonStr + "\""));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(2, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals(PARAM_NAME_0, row.get("name"));
				assertEquals(pv0, row.get("value"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals(PARAM_NAME_1, row.get("name"));
				assertEquals(pv1, row.get("value"));
			}
		}
	}

	@Test
	public void resolveTest_REQUEST_CONTENT_TYPE_JSON() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "参数值1:\"---\\---\r---\n";
		String pv1JsonStr = "参数值1:\\\"---\\\\---\\r---\\n";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.STRING, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testJson");
		dataSet.setRequestContentType(HttpDataSet.REQUEST_CONTENT_TYPE_JSON);
		dataSet.setRequestContent("[ { name: \"" + PARAM_NAME_0 + "\", value: \"" + pv0 + "\" }, { name: \""
				+ PARAM_NAME_1 + "\", value: \"${param}\" } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("value: \"" + pv1JsonStr + "\""));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(2, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals(PARAM_NAME_0, row.get("name"));
				assertEquals(pv0, row.get("value"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals(PARAM_NAME_1, row.get("name"));
				assertEquals(pv1, row.get("value"));
			}
		}
	}

	@Test
	public void resolveTest_REQUEST_CONTENT_TYPE_TEXT() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "参数\"值1";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.STRING, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testText");
		dataSet.setRequestContentCharset("GBK");
		dataSet.setRequestContentType(HttpDataSet.REQUEST_CONTENT_TYPE_TEXT);
		dataSet.setRequestContent("value0=" + pv0 + ",value1=${param}");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("value1=" + pv1));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("contentType", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(1, data.size());

			{
				Map<String, Object> row = data.get(0);
				assertEquals("value0=" + pv0 + ",value1=" + pv1 + "", row.get("value"));
				assertEquals("text/plain; charset=GBK", row.get("contentType"));
			}
		}
	}

	@Test
	public void resolveTest_REQUEST_CONTENT_TYPE_TEXT_XML() throws Throwable
	{
		String v0 = "p0";
		String v1 = "参数值1";
		String v2 = "&---<--->---\"---'";
		String v2Escape = "&amp;---&lt;---&gt;---&quot;---&apos;";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("v0", DataSetParam.DataType.NUMBER, true),
				new DataSetParam("v1", DataSetParam.DataType.STRING, true),
				new DataSetParam("v2", DataSetParam.DataType.STRING, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testXml");
		dataSet.setRequestContentCharset("GBK");
		dataSet.setRequestContentType(HttpDataSet.REQUEST_CONTENT_TYPE_TEXT_XML);
		dataSet.setRequestContent("<entity><v0>${v0}</v0><v1>${v1}</v1><v2>${v2}</v2></entity>");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("v0", v0);
		paramValues.put("v1", v1);
		paramValues.put("v2", v2);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("<v0>" + v0 + "</v0>"));
			assertTrue(templateResult.contains("<v1>" + v1 + "</v1>"));
			assertTrue(templateResult.contains("<v2>" + v2Escape + "</v2>"));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("contentType", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(1, data.size());

			{
				Map<String, Object> row = data.get(0);
				assertEquals("<entity><v0>" + v0 + "</v0><v1>" + v1 + "</v1><v2>" + v2Escape + "</v2></entity>",
						row.get("value"));
				assertEquals("text/xml; charset=GBK", row.get("contentType"));
			}
		}
	}

	@Test
	public void resolveTest_setHeaderContent() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "p1";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testHeader");
		dataSet.setHeaderContent("[ { name: '" + PARAM_NAME_0 + "', value: '" + pv0 + "' }, { name: '" + PARAM_NAME_1
				+ "', value: '${param}' } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf(paramValues));
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();
		String templateResult = result.getTemplateResult();

		{
			assertEquals(2, properties.size());

			assertTrue(templateResult.contains("value: '" + pv1 + "'"));

			{
				DataSetProperty property = properties.get(0);
				assertEquals("name", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}

			{
				DataSetProperty property = properties.get(1);
				assertEquals("value", property.getName());
				assertEquals(DataSetProperty.DataType.STRING, property.getType());
			}
		}

		{
			assertEquals(2, data.size());

			{
				Map<String, Object> row = data.get(0);

				assertEquals(PARAM_NAME_0, row.get("name"));
				assertEquals(pv0, row.get("value"));
			}

			{
				Map<String, Object> row = data.get(1);

				assertEquals(PARAM_NAME_1, row.get("name"));
				assertEquals(pv1, row.get("value"));
			}
		}
	}

	@Test
	public void resolveTest_chineseInUri() throws Throwable
	{
		// 默认
		{
			HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
					SERVER + "/testGetChineseParam?p=中文");
			dataSet.setRequestMethod(HttpDataSet.REQUEST_METHOD_GET);

			TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
			DataSetResult dr = result.getResult();
			@SuppressWarnings("unchecked")
			Map<String, String> data = (Map<String, String>) dr.getData();
			String v = data.get("q");

			assertNotEquals("p=中文", v);
		}

		// false
		{
			HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
					SERVER + "/testGetChineseParam?p=中文");
			dataSet.setRequestMethod(HttpDataSet.REQUEST_METHOD_GET);
			dataSet.setEncodeUri(false);

			TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
			DataSetResult dr = result.getResult();
			@SuppressWarnings("unchecked")
			Map<String, String> data = (Map<String, String>) dr.getData();
			String v = data.get("q");

			assertNotEquals("p=中文", v);
		}

		// true
		{
			HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
					SERVER + "/testGetChineseParam?p=中文");
			dataSet.setRequestMethod(HttpDataSet.REQUEST_METHOD_GET);
			dataSet.setEncodeUri(true);

			TemplateResolvedDataSetResult result = dataSet.resolve(DataSetQuery.valueOf());
			DataSetResult dr = result.getResult();
			@SuppressWarnings("unchecked")
			Map<String, String> data = (Map<String, String>) dr.getData();
			String v = data.get("q");

			assertEquals("p=中文", v);
		}
	}

	protected static Map<String, String> parseRequestParams(ClassicHttpRequest request) throws IOException
	{
		Map<String, String> map = new HashMap<>();

		String content = getRequestStringContent(request);

		String[] strss = content.split("&");
		for (String strs : strss)
		{
			String[] pv = strs.split("=");

			map.put(pv[0], URLDecoder.decode(pv[1], IOUtil.CHARSET_UTF_8));
		}

		return map;
	}

	protected static String getRequestStringContent(ClassicHttpRequest request) throws IOException
	{
		HttpEntity entity = request.getEntity();
		String contentTypeStr = entity.getContentType();
		ContentType contentType = ContentType.parse(contentTypeStr);
		Reader reader = new InputStreamReader(entity.getContent(), contentType.getCharset());
		String content = IOUtil.readString(reader, false);

		return content;
	}

	protected static String toJsonString(Object o) throws IOException
	{
		return JsonSupport.generate(o);
	}
}
