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

package org.datagear.util;

import org.junit.Test;

/**
 * {@linkplain IDUtil}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class IDUtilTest
{
	@Test
	public void randomIdOnTime20Test()
	{
		for (int i = 0; i < 100; i++)
		{
			String id = IDUtil.randomIdOnTime20();
			System.out.println(id);
		}
	}

	@Test
	public void randomTest()
	{
		for (int i = 0; i < 100; i++)
		{
			String id = IDUtil.random(20);
			System.out.println(id);
		}
	}
}
