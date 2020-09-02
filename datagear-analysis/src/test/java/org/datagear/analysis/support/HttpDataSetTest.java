/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.datagear.analysis.DataSetProperty;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
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

	protected static final String PARAM_0 = "param0";

	protected static HttpServer server;

	protected static CloseableHttpClient httpClient;

	@BeforeClass
	public static void initTestHttpServer() throws Throwable
	{
		server = ServerBootstrap.bootstrap()
				.setListenerPort(PORT).register("/test0", new HttpRequestHandler()
				{
					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
							throws HttpException, IOException
					{
						Map<String, String> params = parseRequestParams(request);
						String pv = params.get(PARAM_0);

						StringEntity responseEntity = new StringEntity(
								"[{name: 'aaa', value: 11}, {name: '" + pv + "', value: 22}]",
								ContentType.APPLICATION_JSON);
						response.setEntity(responseEntity);
					}
				}).create();

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
	public void resolveTest_CONTENT_TYPE_FORM() throws Throwable
	{
		String pv = "ppp0";

		HttpDataSet dataSet = new HttpDataSet("a", "a", httpClient, "http://localhost:" + PORT + "/test0");
		dataSet.setRequestContent("[ { name: '" + PARAM_0 + "', value: '" + pv + "' }, { name: 'p1', value: 'b' } ]");

		TemplateResolvedDataSetResult result = dataSet.resolve(Collections.emptyMap());
		List<DataSetProperty> properties = result.getProperties();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> data = (List<Map<String, Object>>) result.getResult().getData();

		{
			assertEquals(2, properties.size());

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

				assertEquals(pv, row.get("name"));
				assertEquals(22, ((Number) row.get("value")).intValue());
			}
		}
	}

	protected static Map<String, String> parseRequestParams(ClassicHttpRequest request) throws IOException
	{
		Map<String, String> map = new HashMap<String, String>();

		HttpEntity entity = request.getEntity();
		String encoding = entity.getContentEncoding();
		Reader reader = new InputStreamReader(entity.getContent(),
				(StringUtil.isEmpty(encoding) ? "iso-8859-1" : encoding));
		String content = IOUtil.readString(reader, false);

		String[] strss = content.split("&");
		for (String strs : strss)
		{
			String[] pv = strs.split("=");

			map.put(pv[0], pv[1]);
		}

		return map;
	}
}
