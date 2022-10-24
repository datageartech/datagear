/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
