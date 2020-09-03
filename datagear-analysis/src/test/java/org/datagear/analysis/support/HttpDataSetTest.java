/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

						StringEntity responseEntity = new StringEntity("[{name: '" + PARAM_NAME_0 + "', value: '" + p0
								+ "'}, {name: '" + PARAM_NAME_1 + "', value: '" + p1 + "'}]",
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

		TemplateResolvedDataSetResult result = dataSet.resolve(paramValues);
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
	public void resolveTest_rqeuestContent_CONTENT_TYPE_FORM() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "参数值1";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testParam");
		dataSet.setRequestContent("[ { name: '" + PARAM_NAME_0 + "', value: '" + pv0 + "' }, { name: '" + PARAM_NAME_1
				+ "', value: '${param}' } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(paramValues);
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
	public void resolveTest_rqeuestContent_CONTENT_TYPE_JSON() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "参数值1";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testJson");
		dataSet.setRequestContentType(HttpDataSet.CONTENT_TYPE_JSON);
		dataSet.setRequestContent("[ { name: '" + PARAM_NAME_0 + "', value: '" + pv0 + "' }, { name: '" + PARAM_NAME_1
				+ "', value: '${param}' } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(paramValues);
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
	public void resolveTest_headerContent() throws Throwable
	{
		String pv0 = "p0";
		String pv1 = "p1";

		List<DataSetParam> params = Arrays.asList(new DataSetParam("param", DataSetParam.DataType.NUMBER, true));

		HttpDataSet dataSet = new HttpDataSet(HttpDataSet.class.getName(), HttpDataSet.class.getName(), httpClient,
				SERVER + "/testHeader");
		dataSet.setRequestContentType(HttpDataSet.CONTENT_TYPE_JSON);
		dataSet.setHeaderContent("[ { name: '" + PARAM_NAME_0 + "', value: '" + pv0 + "' }, { name: '" + PARAM_NAME_1
				+ "', value: '${param}' } ]");
		dataSet.setParams(params);

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("param", pv1);

		TemplateResolvedDataSetResult result = dataSet.resolve(paramValues);
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
}
