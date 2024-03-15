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

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain WebUtils}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class WebUtilsTest
{
	@Test
	public void isHttpSchemeTest()
	{
		assertTrue(WebUtils.isHttpScheme("http"));
		assertTrue(WebUtils.isHttpScheme("HTTP"));
		assertFalse(WebUtils.isHttpScheme("https"));
	}

	@Test
	public void isSecureHttpSchemeTest()
	{
		assertTrue(WebUtils.isSecureHttpScheme("https"));
		assertTrue(WebUtils.isSecureHttpScheme("HTTPS"));
		assertFalse(WebUtils.isSecureHttpScheme("http"));
	}

	@Test
	public void resolvePathAfterTest()
	{
		{
			String url = "/a/b/c";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c#anchor";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c?p=v";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c;p=v";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c;p=v#anchor";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c?p=v#anchor";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c;p=v?ppp=vvv";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}

		{
			String url = "/a/b/c;p=v?ppp=vvv#anchor";
			String re = WebUtils.resolvePathAfter(url, "/a");
			assertEquals("/b/c", re);
		}
	}

	@Test
	public void addUrlParamTest()
	{
		{
			{
				String url = "/a/b";
				String re = WebUtils.addUrlParam(url, "p", "v");
				assertEquals("/a/b?p=v", re);
			}

			{
				String url = "/a/b?p0=v0";
				String re = WebUtils.addUrlParam(url, "p", "v");
				assertEquals("/a/b?p0=v0&p=v", re);
			}

			{
				String url = "/a/b#anchor";
				String re = WebUtils.addUrlParam(url, "p", "v");
				assertEquals("/a/b?p=v#anchor", re);
			}

			{
				String url = "/a/b?p0=v0#anchor";
				String re = WebUtils.addUrlParam(url, "p", "v");
				assertEquals("/a/b?p0=v0&p=v#anchor", re);
			}
		}

		{
			{
				String url = "/a/b";
				String re = WebUtils.addUrlParam(url, null);
				assertEquals("/a/b", re);
			}

			{
				String url = "/a/b";
				String re = WebUtils.addUrlParam(url, "");
				assertEquals("/a/b", re);
			}

			{
				String url = "/a/b#anchor";
				String re = WebUtils.addUrlParam(url, "q=v");
				assertEquals("/a/b?q=v#anchor", re);
			}

			{
				String url = "/a/b";
				String re = WebUtils.addUrlParam(url, "q=v");
				assertEquals("/a/b?q=v", re);
			}

			{
				String url = "/a/b?q0=v0";
				String re = WebUtils.addUrlParam(url, "q=v");
				assertEquals("/a/b?q0=v0&q=v", re);
			}

			{
				String url = "/a/b?q0=v0#anchor";
				String re = WebUtils.addUrlParam(url, "q=v");
				assertEquals("/a/b?q0=v0&q=v#anchor", re);
			}
		}
	}
}
