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

package org.datagear.util.i18n;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * {@linkplain LabelUtil}单元测试用例。
 * 
 * @author datagear@163.com
 */
public class LabelUtilTest
{
	@Test
	public void getPriorityStringListTest()
	{
		{
			List<String> pl = LabelUtil.getPriorityStringList(Locale.SIMPLIFIED_CHINESE);
			
			assertEquals(2, pl.size());
			assertEquals("zh_CN", pl.get(0));
			assertEquals("zh", pl.get(1));
		}
		
		{
			List<String> pl = LabelUtil.getPriorityStringList(Locale.US);
			
			assertEquals(2, pl.size());
			assertEquals("en_US", pl.get(0));
			assertEquals("en", pl.get(1));
		}
		
		{
			List<String> pl = LabelUtil.getPriorityStringList(new Locale("zh"));
			
			assertEquals(1, pl.size());
			assertEquals("zh", pl.get(0));
		}
		
		{
			List<String> pl = LabelUtil.getPriorityStringList(new Locale("en"));
			
			assertEquals(1, pl.size());
			assertEquals("en", pl.get(0));
		}
	}
}
