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

package org.datagear.analysis.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * {@linkplain PropertyType}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyTypeTest
{
	@Test
	public void normalizeTest()
	{
		{
			String t = PropertyType.normalize("string", null);
			assertEquals(PropertyType.STRING, t);
		}
		{
			String t = PropertyType.normalize("STRING", null);
			assertEquals(PropertyType.STRING, t);
		}

		{
			String t = PropertyType.normalize("boolean", null);
			assertEquals(PropertyType.BOOLEAN, t);
		}
		{
			String t = PropertyType.normalize("BOOLEAN", null);
			assertEquals(PropertyType.BOOLEAN, t);
		}

		{
			String t = PropertyType.normalize("integer", null);
			assertEquals(PropertyType.INTEGER, t);
		}
		{
			String t = PropertyType.normalize("INTEGER", null);
			assertEquals(PropertyType.INTEGER, t);
		}

		{
			String t = PropertyType.normalize("number", null);
			assertEquals(PropertyType.NUMBER, t);
		}
		{
			String t = PropertyType.normalize("NUMBER", null);
			assertEquals(PropertyType.NUMBER, t);
		}

		{
			String t = PropertyType.normalize("object", null);
			assertEquals(PropertyType.OBJECT, t);
		}
		{
			String t = PropertyType.normalize("OBJECT", null);
			assertEquals(PropertyType.OBJECT, t);
		}

		{
			String t = PropertyType.normalize(null, null);
			assertNull(t);
		}

		{
			String t = PropertyType.normalize("sdf", null);
			assertNull(t);
		}

		{
			String t = PropertyType.normalize("sdf", "sdf");
			assertEquals("sdf", t);
		}
	}
}
