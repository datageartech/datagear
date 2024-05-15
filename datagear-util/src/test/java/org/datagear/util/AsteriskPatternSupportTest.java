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

package org.datagear.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain AsteriskPatternSupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class AsteriskPatternSupportTest
{
	@Test
	public void findKeyMatchedTest_map_collection_true()
	{
		AsteriskPatternSupport aps = new AsteriskPatternSupport(new AsteriskPatternMatcher(true));

		Map<String, String> map = new HashMap<String, String>();
		map.put("abc", "aabbcc");
		map.put("def", "ddeeff");
		map.put("ghi", "gghhii");

		{
			List<String> params = Arrays.asList("*b*", "*e*");
			String actual = aps.findKeyMatched(map, params, true);

			assertEquals("aabbcc", actual);
		}

		{
			List<String> params = Arrays.asList("*uuu*", "*e*");
			String actual = aps.findKeyMatched(map, params, true);

			assertEquals("ddeeff", actual);
		}

		{
			List<String> params = Arrays.asList("*uuu*", "regex:^\\w+e\\w+$");
			String actual = aps.findKeyMatched(map, params, true);

			assertEquals("ddeeff", actual);
		}

		{
			List<String> params = Arrays.asList("*uuu*", "*vvvv*");
			String actual = aps.findKeyMatched(map, params, true);

			assertNull(actual);
		}

		{
			List<String> params = Arrays.asList("");
			String actual = aps.findKeyMatched(map, params, true);

			assertNull(actual);
		}
	}

	@Test
	public void findKeyMatchedTest_map_collection_false()
	{
		AsteriskPatternSupport aps = new AsteriskPatternSupport(new AsteriskPatternMatcher(true));

		Map<String, String> map = new HashMap<String, String>();
		map.put("*b*", "aabbcc");
		map.put("*e*", "ddeeff");
		map.put("*h*", "gghhii");
		map.put("regex:^\\d+z\\d+$", "zzzzzz");

		{
			List<String> params = Arrays.asList("abc", "def");
			String actual = aps.findKeyMatched(map, params, false);

			assertEquals("aabbcc", actual);
		}

		{
			List<String> params = Arrays.asList("uuu", "def");
			String actual = aps.findKeyMatched(map, params, false);

			assertEquals("ddeeff", actual);
		}

		{
			List<String> params = Arrays.asList("uuu", "6z6");
			String actual = aps.findKeyMatched(map, params, false);

			assertEquals("zzzzzz", actual);
		}

		{
			List<String> params = Arrays.asList("uuu", "vvvv");
			String actual = aps.findKeyMatched(map, params, false);

			assertNull(actual);
		}

		{
			List<String> params = Arrays.asList("");
			String actual = aps.findKeyMatched(map, params, false);

			assertNull(actual);
		}
	}

	@Test
	public void findKeyMatchedTest_list_collection_true()
	{
		AsteriskPatternSupport aps = new AsteriskPatternSupport(new AsteriskPatternMatcher(true));

		List<TextKeyValuePair<String>> list = new ArrayList<TextKeyValuePair<String>>();
		list.add(new TextKeyValuePair<String>("abc", "aabbcc"));
		list.add(new TextKeyValuePair<String>("def", "ddeeff"));
		list.add(new TextKeyValuePair<String>("ghi", "gghhii"));

		{
			List<String> params = Arrays.asList("*b*", "*e*");
			String actual = aps.findKeyMatched(list, params, true);

			assertEquals("aabbcc", actual);
		}

		{
			List<String> params = Arrays.asList("*uuu*", "*e*");
			String actual = aps.findKeyMatched(list, params, true);

			assertEquals("ddeeff", actual);
		}

		{
			List<String> params = Arrays.asList("*uuu*", "*vvvv*");
			String actual = aps.findKeyMatched(list, params, true);

			assertNull(actual);
		}

		{
			List<String> params = Arrays.asList("");
			String actual = aps.findKeyMatched(list, params, true);

			assertNull(actual);
		}
	}

	@Test
	public void findKeyMatchedTest_list_collection_false()
	{
		AsteriskPatternSupport aps = new AsteriskPatternSupport(new AsteriskPatternMatcher(true));

		List<TextKeyValuePair<String>> list = new ArrayList<TextKeyValuePair<String>>();
		list.add(new TextKeyValuePair<String>("*b*", "aabbcc"));
		list.add(new TextKeyValuePair<String>("*e*", "ddeeff"));
		list.add(new TextKeyValuePair<String>("*h*", "gghhii"));

		{
			List<String> params = Arrays.asList("abc", "def");
			String actual = aps.findKeyMatched(list, params, false);

			assertEquals("aabbcc", actual);
		}

		{
			List<String> params = Arrays.asList("uuu", "def");
			String actual = aps.findKeyMatched(list, params, false);

			assertEquals("ddeeff", actual);
		}

		{
			List<String> params = Arrays.asList("uuu", "vvvv");
			String actual = aps.findKeyMatched(list, params, false);

			assertNull(actual);
		}

		{
			List<String> params = Arrays.asList("");
			String actual = aps.findKeyMatched(list, params, false);

			assertNull(actual);
		}
	}
}
