/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import org.junit.Assert;
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
	public void isValidSubContextPathTest()
	{
		Assert.assertFalse(WebContextPath.isValidSubContextPath(null));
		Assert.assertTrue(WebContextPath.isValidSubContextPath(""));
		Assert.assertTrue(WebContextPath.isValidSubContextPath("/abc"));
		Assert.assertTrue(WebContextPath.isValidSubContextPath("/abc/def"));
		Assert.assertTrue(WebContextPath.isValidSubContextPath("/abc/def/ghi"));
		Assert.assertFalse(WebContextPath.isValidSubContextPath("/"));
		Assert.assertFalse(WebContextPath.isValidSubContextPath("////"));
		Assert.assertFalse(WebContextPath.isValidSubContextPath("/abc/"));
	}
}
