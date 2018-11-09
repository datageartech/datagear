/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain PropertyPath}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyPathTest
{
	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test()
	{
		{
			String pp = "order.product<0>";
			PropertyPath propertyPath = PropertyPath.valueOf(pp);

			Assert.assertEquals(2, propertyPath.length());

			Assert.assertTrue(propertyPath.isProperty(0));
			Assert.assertTrue(propertyPath.isPropertyHead());

			Assert.assertTrue(propertyPath.isProperty(1));
			Assert.assertTrue(propertyPath.isPropertyTail());
			Assert.assertTrue(propertyPath.hasPropertyModelIndex(1));
			Assert.assertTrue(propertyPath.hasPropertyModelIndexTail());
			Assert.assertEquals(0, propertyPath.getPropertyModelIndex(1));
			Assert.assertEquals(0, propertyPath.getPropertyModelIndexTail());

			Assert.assertEquals(pp, propertyPath.toString());
		}

		{
			String pp = "[0].order.product<0>";
			PropertyPath propertyPath = PropertyPath.valueOf(pp);

			Assert.assertEquals(3, propertyPath.length());

			Assert.assertTrue(propertyPath.isElement(0));
			Assert.assertTrue(propertyPath.isElementHead());
			Assert.assertEquals(0, propertyPath.getElementIndex(0));
			Assert.assertEquals(0, propertyPath.getElementIndexHead());

			Assert.assertTrue(propertyPath.isProperty(1));
			Assert.assertEquals("order", propertyPath.getPropertyName(1));

			Assert.assertTrue(propertyPath.isProperty(2));
			Assert.assertTrue(propertyPath.isPropertyTail());
			Assert.assertEquals("product", propertyPath.getPropertyName(2));
			Assert.assertEquals("product", propertyPath.getPropertyNameTail());
			Assert.assertTrue(propertyPath.hasPropertyModelIndex(2));
			Assert.assertTrue(propertyPath.hasPropertyModelIndexTail());
			Assert.assertEquals(0, propertyPath.getPropertyModelIndex(2));
			Assert.assertEquals(0, propertyPath.getPropertyModelIndexTail());

			Assert.assertEquals(pp, propertyPath.toString());
		}

		{
			String pp = "prop\\.erty.prop\\[erty.prop\\]erty.prop\\<erty.prop\\>erty.prop\\erty";
			PropertyPath propertyPath = PropertyPath.valueOf(pp);

			Assert.assertEquals(6, propertyPath.length());

			Assert.assertEquals("prop.erty", propertyPath.getPropertyName(0));

			Assert.assertEquals("prop[erty", propertyPath.getPropertyName(1));

			Assert.assertEquals("prop]erty", propertyPath.getPropertyName(2));

			Assert.assertEquals("prop<erty", propertyPath.getPropertyName(3));

			Assert.assertEquals("prop>erty", propertyPath.getPropertyName(4));

			Assert.assertEquals("prop\\erty", propertyPath.getPropertyName(5));
		}
	}

	@Test
	public void escapePropertyNameTest()
	{
		String pn = "p.r[o]p<e>rt\\y";
		String epn = PropertyPath.escapePropertyName(pn);

		Assert.assertEquals("p\\.r\\[o\\]p\\<e\\>rt\\y", epn);
	}

	@Test
	public void unescapePropertyNameTest()
	{
		{
			String epn = "p\\.r\\[o\\]p\\<e\\>rt\\y";
			String pn = PropertyPath.unescapePropertyName(epn);

			Assert.assertEquals("p.r[o]p<e>rt\\y", pn);
		}
	}
}
