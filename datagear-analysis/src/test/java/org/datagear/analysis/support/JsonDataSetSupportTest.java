/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain JsonDataSetSupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataSetSupportTest
{
	private JsonDataSetSupport jsonDataSetSupport = new JsonDataSetSupport();

	@Test
	public void resolveResultDataTest_String()
	{
		Object data = jsonDataSetSupport.resolveResultData("{name:'a', value: 3}");

		Assert.assertTrue(data instanceof Map);

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) data;

		Assert.assertEquals("a", map.get("name"));
		Assert.assertEquals(3, ((Number) map.get("value")).intValue());
	}
}
