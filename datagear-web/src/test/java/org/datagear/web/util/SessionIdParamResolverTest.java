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

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;

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
}
