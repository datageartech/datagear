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

package org.datagear.util.function;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain OnceSupplierTest}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class OnceSupplierTest
{
	@Test
	public void getTest()
	{
		{
			OnceSupplier<Object> s = new OnceSupplier<>(() ->
			{
				return new Object();
			});

			Object expected = s.get();
			Object actual0 = s.get();
			Object actual1 = s.get();

			assertTrue(expected == actual0);
			assertTrue(actual0 == actual1);
		}

		{
			OnceSupplier<Object> s = new OnceSupplier<>();

			assertNull(s.get());
		}
	}
}
