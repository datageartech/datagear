/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.i18n;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain Label}单元测试用例。
 * 
 * @author datagear@163.com
 */
public class LabelTest
{
	@Test
	public void getValueTest()
	{
		{
			Map<String, String> localeValues = new HashMap<String, String>();
			localeValues.put("zh_CN", "zh_CN");
			localeValues.put("zh", "zh");
			localeValues.put("en_US", "en_US");
			localeValues.put("en", "en");
			
			Label label = new Label("default", localeValues);
			
			assertEquals("zh_CN", label.getValue(new Locale("zh", "CN", "zzz")));
			assertEquals("zh_CN", label.getValue(Locale.SIMPLIFIED_CHINESE));
			assertEquals("zh", label.getValue(new Locale("zh")));
			
			assertEquals("en_US", label.getValue(new Locale("en", "US", "zzz")));
			assertEquals("en_US", label.getValue(Locale.US));
			assertEquals("en", label.getValue(new Locale("en")));
			
			assertEquals("default", label.getValue(new Locale("a", "b", "c")));
		}
		
		{
			Label label = new Label(null);
			assertEquals("", label.getValue(Locale.SIMPLIFIED_CHINESE));
		}
	}
	
	@Test
	public void getStringPriorityListTest()
	{
		{
			List<String> pl = Label.getStringPriorityList(Locale.SIMPLIFIED_CHINESE);
			
			assertEquals(2, pl.size());
			assertEquals("zh_CN", pl.get(0));
			assertEquals("zh", pl.get(1));
		}
		
		{
			List<String> pl = Label.getStringPriorityList(Locale.US);
			
			assertEquals(2, pl.size());
			assertEquals("en_US", pl.get(0));
			assertEquals("en", pl.get(1));
		}
		
		{
			List<String> pl = Label.getStringPriorityList(new Locale("zh"));
			
			assertEquals(1, pl.size());
			assertEquals("zh", pl.get(0));
		}
		
		{
			List<String> pl = Label.getStringPriorityList(new Locale("en"));
			
			assertEquals(1, pl.size());
			assertEquals("en", pl.get(0));
		}
	}
}
