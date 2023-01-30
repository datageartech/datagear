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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartPluginAttribute;
import org.junit.Test;

/**
 * {@linkplain ChartPluginAttributeValueConverter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginAttributeValueConverterTest
{
	public ChartPluginAttributeValueConverterTest()
	{
		super();
	}

	@Test
	public void convertTest()
	{
		List<ChartPluginAttribute> chartPluginAttributes = new ArrayList<>();
		chartPluginAttributes.add(new ChartPluginAttribute("name", ChartPluginAttribute.DataType.STRING, true));
		chartPluginAttributes.add(new ChartPluginAttribute("size", ChartPluginAttribute.DataType.NUMBER, true));
		chartPluginAttributes.add(new ChartPluginAttribute("enable", ChartPluginAttribute.DataType.BOOLEAN, true));
		
		ChartPluginAttributeValueConverter converter = new ChartPluginAttributeValueConverter();

		{
			Map<String, Object> actual = converter.convert(null, chartPluginAttributes);
			assertNull(actual);
		}
		
		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", "aaa");
			values.put("size", "3");
			values.put("enable", "true");
			values.put("other0", 5);
			values.put("other1", "false");

			Map<String, Object> actual = converter.convert(values, chartPluginAttributes);

			assertEquals("aaa", actual.get("name"));
			assertEquals(3, ((Integer) actual.get("size")).intValue());
			assertEquals(true, ((Boolean) actual.get("enable")).booleanValue());
			assertEquals(5, ((Integer) actual.get("other0")).intValue());
			assertEquals("false", actual.get("other1"));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", new String[] { "aaa", "bbb" });
			values.put("size", new String[] { "3", "4" });
			values.put("enable", new String[] { "true", "false" });
			values.put("other0", new int[] { 5, 6 });
			values.put("other1", new boolean[] { false, false });

			Map<String, Object> actual = converter.convert(values, chartPluginAttributes);
			
			Object[] actualName = (Object[])actual.get("name");
			Object[] actualSize = (Object[])actual.get("size");
			Object[] actualEnable = (Object[])actual.get("enable");
			int[] actualOther0 = (int[])actual.get("other0");
			boolean[] actualOther1 = (boolean[])actual.get("other1");

			assertEquals(2, actualName.length);
			assertEquals("aaa", actualName[0]);
			assertEquals("bbb", actualName[1]);

			assertEquals(2, actualSize.length);
			assertEquals(3, ((Integer) actualSize[0]).intValue());
			assertEquals(4, ((Integer) actualSize[1]).intValue());

			assertEquals(2, actualEnable.length);
			assertEquals(true, ((Boolean) actualEnable[0]).booleanValue());
			assertEquals(false, ((Boolean) actualEnable[1]).booleanValue());

			assertEquals(2, actualOther0.length);
			assertEquals(5, actualOther0[0]);
			assertEquals(6, actualOther0[1]);

			assertEquals(2, actualOther1.length);
			assertEquals(false, actualOther1[0]);
			assertEquals(false, actualOther1[1]);
		}
	}
}
