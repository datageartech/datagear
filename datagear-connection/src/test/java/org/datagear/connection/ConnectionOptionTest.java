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

package org.datagear.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * {@linkplain ConnectionOption}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ConnectionOptionTest
{
	@Test
	public void copyOfPsdMaskTest()
	{
		{
			ConnectionOption co = new ConnectionOption("jdbc:");
			ConnectionOption actual = co.copyOfPsdMask();

			assertEquals(co, actual);
			assertFalse(actual == co);
			assertFalse(co.getProperties() == actual.getProperties());
		}

		{
			ConnectionOption co = new ConnectionOption("jdbc:", "test", "test");
			ConnectionOption actual = co.copyOfPsdMask();

			assertEquals(co.getUrl(), actual.getUrl());
			assertEquals(co.getUser(), actual.getUser());
			assertNotEquals(co.getPassword(), actual.getPassword());
			assertEquals("t***t", actual.getPassword());
			assertFalse(actual == co);
			assertFalse(co.getProperties() == actual.getProperties());
		}

		{
			ConnectionOption co = new ConnectionOption("jdbc:", "test", "test");
			ConnectionOption actual = co.copyOfPsdMask();

			assertEquals(co.getUrl(), actual.getUrl());
			assertEquals(co.getUser(), actual.getUser());
			assertNotEquals(co.getPassword(), actual.getPassword());
			assertEquals("t***t", actual.getPassword());
			assertFalse(actual == co);
			assertFalse(co.getProperties() == actual.getProperties());
		}
	}
}
