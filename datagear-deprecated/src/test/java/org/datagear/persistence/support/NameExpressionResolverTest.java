/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.util.List;

import org.datagear.util.expression.ExpressionResolver;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain ExpressionResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class NameExpressionResolverTest
{
	@Test
	public void resolveTest()
	{
		NameExpressionResolver expressionResolver = new NameExpressionResolver();

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("${name:content}");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${name:content}".length(), e.getEndIndex());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("prefix${name:content}");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(6, e.getStartIndex());
			Assert.assertEquals(6 + "${name:content}".length(), e.getEndIndex());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("${name:content}suffix");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${name:content}".length(), e.getEndIndex());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("prefix${name:content}suffix");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(6, e.getStartIndex());
			Assert.assertEquals(6 + "${name:content}".length(), e.getEndIndex());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("${ name : content }");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("${ name : content }", e.getExpression());
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${ name : content }".length(), e.getEndIndex());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<NameExpression> expressions = expressionResolver
					.resolveNameExpressions("prefix${content0}gap${name1 : content1}gap${ name2: content2 }sufix");

			Assert.assertEquals(3, expressions.size());

			{
				NameExpression e = expressions.get(0);
				Assert.assertEquals("${content0}", e.getExpression());
				Assert.assertEquals(6, e.getStartIndex());
				Assert.assertEquals(6 + "${content0}".length(), e.getEndIndex());
				Assert.assertFalse(e.hasName());
				Assert.assertEquals("content0", e.getContent());
			}

			{
				NameExpression e = expressions.get(1);
				Assert.assertEquals("${name1 : content1}", e.getExpression());
				Assert.assertEquals("prefix${content0}gap".length(), e.getStartIndex());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}".length(), e.getEndIndex());
				Assert.assertEquals("name1", e.getName());
				Assert.assertEquals("content1", e.getContent());
			}

			{
				NameExpression e = expressions.get(2);
				Assert.assertEquals("${ name2: content2 }", e.getExpression());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}gap".length(), e.getStartIndex());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}gap${ name2: content2 }".length(),
						e.getEndIndex());
				Assert.assertEquals("name2", e.getName());
				Assert.assertEquals("content2", e.getContent());
			}
		}

		{
			List<NameExpression> expressions = expressionResolver.resolveNameExpressions("prefix${cont\\:ent\\}0}");

			Assert.assertEquals(1, expressions.size());

			NameExpression e = expressions.get(0);
			Assert.assertEquals("cont:ent}0", e.getContent());
		}
	}
}
