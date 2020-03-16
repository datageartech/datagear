/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * {@linkplain SpelExpressionParser}测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SpelExpressionParserTest
{
	private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

	@Test
	public void test()
	{
		{
			int value = (Integer) spelExpressionParser.parseExpression("index").getValue(new TestBean());
			Assert.assertEquals(0, value);
		}

		{
			int value = (Integer) spelExpressionParser.parseExpression("index").getValue(new TestBean(2));
			Assert.assertEquals(2, value);
		}

		{
			int value = (Integer) spelExpressionParser.parseExpression("index * 5").getValue(new TestBean(2));
			Assert.assertEquals(10, value);
		}
	}

	protected static class TestBean
	{
		private int index;

		public TestBean()
		{
			super();
		}

		public TestBean(int index)
		{
			super();
			this.index = index;
		}

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}
	}
}
