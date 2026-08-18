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

package org.datagear.analysis.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain ChartPluginIdSpec}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginIdSpecTest
{
	private ChartPluginIdSpec spec = new ChartPluginIdSpec();

	@Test
	public void isValidIdTest()
	{
		{
			String id = null;
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "";
			for (int i = 0; i < 101; i++)
				id += "a";

			assertFalse(spec.isValidId(id));
		}

		{
			String id = "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789-.-_$";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "  ";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "\t";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "<";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "'";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "\"";
			assertFalse(spec.isValidId(id));
		}

		{
			String id = "";
			for (int i = 0; i < 100; i++)
				id += "a";

			assertTrue(spec.isValidId(id));
		}

		{
			String id = "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789-.-_";
			assertTrue(spec.isValidId(id));
		}
	}

}
