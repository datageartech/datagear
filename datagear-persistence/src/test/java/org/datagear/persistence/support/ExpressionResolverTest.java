/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.util.Arrays;
import java.util.List;

import org.datagear.persistence.support.ExpressionResolver.Expression;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain ExpressionResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ExpressionResolverTest
{
	@Test
	public void isExpressionTest()
	{
		ExpressionResolver expressionResolver = new ExpressionResolver();

		Assert.assertTrue(expressionResolver.isExpression("${content}"));
		Assert.assertTrue(expressionResolver.isExpression("abcdef${content}"));
		Assert.assertTrue(expressionResolver.isExpression("${content}ghi"));
		Assert.assertTrue(expressionResolver.isExpression("abcdef${content}ghi"));

		Assert.assertTrue(expressionResolver.isExpression("${name:content}"));
		Assert.assertTrue(expressionResolver.isExpression("abcdef${name:content}"));
		Assert.assertTrue(expressionResolver.isExpression("${name:content}ghi"));
		Assert.assertTrue(expressionResolver.isExpression("abcdef${name:content}ghi"));

		Assert.assertFalse(expressionResolver.isExpression("${name:}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${name:}ghi"));

		Assert.assertFalse(expressionResolver.isExpression("${:content}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${:content}ghi"));

		Assert.assertFalse(expressionResolver.isExpression("${name:content"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${name:content"));

		Assert.assertFalse(expressionResolver.isExpression("${}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${}"));
		Assert.assertFalse(expressionResolver.isExpression("${}ghi"));

		Assert.assertFalse(expressionResolver.isExpression("${:}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${:}"));
		Assert.assertFalse(expressionResolver.isExpression("${:}ghi"));
	}

	@Test
	public void resolveTest()
	{
		ExpressionResolver expressionResolver = new ExpressionResolver();

		{
			List<Expression> expressions = expressionResolver.resolve("${content}");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${content}", e.getExpression());
			Assert.assertEquals(0, e.getStart());
			Assert.assertEquals("${content}".length(), e.getEnd());
			Assert.assertFalse(e.hasName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("${name:content}");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(0, e.getStart());
			Assert.assertEquals("${name:content}".length(), e.getEnd());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix${name:content}");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(6, e.getStart());
			Assert.assertEquals(6 + "${name:content}".length(), e.getEnd());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("${name:content}suffix");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(0, e.getStart());
			Assert.assertEquals("${name:content}".length(), e.getEnd());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix${name:content}suffix");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${name:content}", e.getExpression());
			Assert.assertEquals(6, e.getStart());
			Assert.assertEquals(6 + "${name:content}".length(), e.getEnd());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("${ name : content }");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${ name : content }", e.getExpression());
			Assert.assertEquals(0, e.getStart());
			Assert.assertEquals("${ name : content }".length(), e.getEnd());
			Assert.assertEquals("name", e.getName());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver
					.resolve("prefix${content0}gap${name1 : content1}gap${ name2: content2 }sufix");

			Assert.assertEquals(3, expressions.size());

			{
				Expression e = expressions.get(0);
				Assert.assertEquals("${content0}", e.getExpression());
				Assert.assertEquals(6, e.getStart());
				Assert.assertEquals(6 + "${content0}".length(), e.getEnd());
				Assert.assertFalse(e.hasName());
				Assert.assertEquals("content0", e.getContent());
			}

			{
				Expression e = expressions.get(1);
				Assert.assertEquals("${name1 : content1}", e.getExpression());
				Assert.assertEquals("prefix${content0}gap".length(), e.getStart());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}".length(), e.getEnd());
				Assert.assertEquals("name1", e.getName());
				Assert.assertEquals("content1", e.getContent());
			}

			{
				Expression e = expressions.get(2);
				Assert.assertEquals("${ name2: content2 }", e.getExpression());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}gap".length(), e.getStart());
				Assert.assertEquals("prefix${content0}gap${name1 : content1}gap${ name2: content2 }".length(),
						e.getEnd());
				Assert.assertEquals("name2", e.getName());
				Assert.assertEquals("content2", e.getContent());
			}
		}
	}

	@Test
	public void evaluateTest()
	{
		ExpressionResolver expressionResolver = new ExpressionResolver();

		{
			String source = "${content}";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("a", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "${name:content}";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("a", expressionResolver.evaluate(source, expressions, values, ""));

		}

		{
			String source = "prefix${name:content}";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("prefixa", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "${name:content}suffix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("asuffix", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "prefix${name:content}suffix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("prefixasuffix", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "${ name : content }";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("a", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "prefix${content0}gap${name1 : content1}gap${ name2: content2 }sufix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a", "b", "c");

			Assert.assertEquals("prefixagapbgapcsufix", expressionResolver.evaluate(source, expressions, values, ""));
		}
	}
}
