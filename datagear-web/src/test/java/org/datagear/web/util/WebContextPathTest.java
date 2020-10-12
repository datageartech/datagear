/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * {@linkplain WebContextPath}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class WebContextPathTest
{
	@Test
	public void trimSubContextPathTest()
	{
		assertEquals("", WebContextPath.trimSubContextPath(null));
		assertEquals("", WebContextPath.trimSubContextPath(""));
		assertEquals("/abc", WebContextPath.trimSubContextPath("/abc"));
		assertEquals("/abc/def", WebContextPath.trimSubContextPath("/abc/def"));
		assertEquals("/abc/def/ghi", WebContextPath.trimSubContextPath("/abc/def/ghi"));
		assertEquals("", WebContextPath.trimSubContextPath("/"));
		assertEquals("", WebContextPath.trimSubContextPath("////"));
		assertEquals("/abc", WebContextPath.trimSubContextPath("/abc/"));
		assertEquals("/abc/def", WebContextPath.trimSubContextPath("/abc/def/"));
	}
}
