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

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * {@linkplain SessionIdParamResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SessionIdParamResolverTest
{
	@Test
	public void addSessionIdTest()
	{
		// 默认参数名
		{
			SessionIdParamResolver spr = new SessionIdParamResolver();

			{
				String url = spr.addSessionId("/a/b", "123456");
				assertEquals("/a/b;jsessionid=123456", url);
			}

			{
				String url = spr.addSessionId("/a/b?p=v", "123456");
				assertEquals("/a/b;jsessionid=123456?p=v", url);
			}

			{
				String url = spr.addSessionId("/a/b#anchor", "123456");
				assertEquals("/a/b;jsessionid=123456#anchor", url);
			}

			{
				String url = spr.addSessionId("/a/b?p=v#anchor", "123456");
				assertEquals("/a/b;jsessionid=123456?p=v#anchor", url);
			}
		}

		// 自定义参数名
		{
			SessionIdParamResolver spr = new SessionIdParamResolver();
			spr.setSessionIdParamName("sid");

			{
				String url = spr.addSessionId("/a/b", "123456");
				assertEquals("/a/b;sid=123456", url);
			}

			{
				String url = spr.addSessionId("/a/b?p=v", "123456");
				assertEquals("/a/b;sid=123456?p=v", url);
			}

			{
				String url = spr.addSessionId("/a/b#anchor", "123456");
				assertEquals("/a/b;sid=123456#anchor", url);
			}

			{
				String url = spr.addSessionId("/a/b?p=v#anchor", "123456");
				assertEquals("/a/b;sid=123456?p=v#anchor", url);
			}
		}
	}

	@Test
	public void resolveSessionIdsTest()
	{
		SessionIdParamResolver spr = new SessionIdParamResolver();

		// 没有
		{
			{
				String url = "/a/b";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b?p=v";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b?p=v#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid?q=v";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid?q=v#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid=";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid=?q=v";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid=#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;jsessionid=?q=v#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
			{
				String url = "/a/b;pp=vv?q=v#anchor";
				List<String> sids = spr.resolveSessionIds(url);
				assertEquals(0, sids.size());
			}
		}

		// 有
		{
			String url = "/a/b;jsessionid=123456";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(1, sids.size());
			assertEquals("123456", sids.get(0));
		}
		{
			String url = "/a/b;jsessionid=123456?q=v";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(1, sids.size());
			assertEquals("123456", sids.get(0));
		}
		{
			String url = "/a/b;jsessionid=123456#anchor";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(1, sids.size());
			assertEquals("123456", sids.get(0));
		}
		{
			String url = "/a/b;jsessionid=123456?q=v#anchor";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(1, sids.size());
			assertEquals("123456", sids.get(0));
		}
		{
			String url = "/a/b;jsessionid=123456;jsessionid=789";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(2, sids.size());
			assertEquals("123456", sids.get(0));
			assertEquals("789", sids.get(1));
		}
		{
			String url = "/a/b;jsessionid=123456;jsessionid=789?q=v";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(2, sids.size());
			assertEquals("123456", sids.get(0));
			assertEquals("789", sids.get(1));
		}
		{
			String url = "/a/b;jsessionid=123456;jsessionid=789#anchor";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(2, sids.size());
			assertEquals("123456", sids.get(0));
			assertEquals("789", sids.get(1));
		}
		{
			String url = "/a/b;jsessionid=123456;jsessionid=789?q=v#anchor";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(2, sids.size());
			assertEquals("123456", sids.get(0));
			assertEquals("789", sids.get(1));
		}
		{
			String url = "/a/b;jsessionid=123456;jsessionid=789;pp=vv?q=v#anchor";
			List<String> sids = spr.resolveSessionIds(url);
			assertEquals(2, sids.size());
			assertEquals("123456", sids.get(0));
			assertEquals("789", sids.get(1));
		}
	}
}
