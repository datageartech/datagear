/*
 * Copyright 2018-2023 datagear.tech
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

import java.util.HashMap;
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
}
