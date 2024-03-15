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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain SimpleLastModifiedService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleLastModifiedServiceTest
{
	@Test
	public void isModifiedTest()
	{
		String name = "a";

		{
			SimpleLastModifiedService service = new SimpleLastModifiedService();

			assertTrue(service.isModified(name, LastModifiedService.LAST_MODIFIED_INIT));
			assertFalse(service.isModified(name, LastModifiedService.LAST_MODIFIED_UNSET));
			assertTrue(service.isModified(name, System.currentTimeMillis()));
		}

		{
			SimpleLastModifiedService service = new SimpleLastModifiedService();

			service.setLastModifiedNow(name);
			assertTrue(service.isModified(name, System.currentTimeMillis() + 1));
		}

		{
			SimpleLastModifiedService service = new SimpleLastModifiedService();

			long now = System.currentTimeMillis();
			service.setLastModified(name, now);
			assertFalse(service.isModified(name, now));
		}
	}
}
