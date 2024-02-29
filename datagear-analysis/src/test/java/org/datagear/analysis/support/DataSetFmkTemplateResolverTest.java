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

package org.datagear.analysis.support;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain DataSetFmkTemplateResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFmkTemplateResolverTest
{
	@Test
	public void resolveTest()
	{
		DataSetFmkTemplateResolver resolver = new DataSetFmkTemplateResolver();
		
		{
			String text = "hello, ${name} !";
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "世界");
			
			String actual = resolver.resolve(text, params);
			
			assertEquals("hello, 世界 !", actual);
		}
		
		//验证【修复数据集参数化模板处理可能会丢失模板信息的BUG】
		{
			String text = "/a/b/c/${value}/";
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("value", "d");
			
			String actual = resolver.resolve(text, params);
			
			assertEquals("/a/b/c/d/", actual);
		}
	}
}
