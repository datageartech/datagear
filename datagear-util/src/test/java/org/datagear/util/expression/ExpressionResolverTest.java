/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util.expression;

import java.util.Arrays;
import java.util.List;

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

		Assert.assertFalse(expressionResolver.isExpression("${}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${}"));
		Assert.assertFalse(expressionResolver.isExpression("${}ghi"));

		Assert.assertFalse(expressionResolver.isExpression("${}"));
		Assert.assertFalse(expressionResolver.isExpression("abcdef${}"));
		Assert.assertFalse(expressionResolver.isExpression("${}ghi"));
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
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${content}".length(), e.getEndIndex());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix${content}");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${content}", e.getExpression());
			Assert.assertEquals(6, e.getStartIndex());
			Assert.assertEquals(6 + "${content}".length(), e.getEndIndex());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("${content}suffix");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${content}", e.getExpression());
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${content}".length(), e.getEndIndex());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix${content}suffix");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${content}", e.getExpression());
			Assert.assertEquals(6, e.getStartIndex());
			Assert.assertEquals(6 + "${content}".length(), e.getEndIndex());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("${ content }");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("${ content }", e.getExpression());
			Assert.assertEquals(0, e.getStartIndex());
			Assert.assertEquals("${ content }".length(), e.getEndIndex());
			Assert.assertEquals("content", e.getContent());
		}

		{
			List<Expression> expressions = expressionResolver
					.resolve("prefix${content0}gap${content1}gap${ content2 }sufix");

			Assert.assertEquals(3, expressions.size());

			{
				Expression e = expressions.get(0);
				Assert.assertEquals("${content0}", e.getExpression());
				Assert.assertEquals(6, e.getStartIndex());
				Assert.assertEquals(6 + "${content0}".length(), e.getEndIndex());
				Assert.assertEquals("content0", e.getContent());
			}

			{
				Expression e = expressions.get(1);
				Assert.assertEquals("${content1}", e.getExpression());
				Assert.assertEquals("prefix${content0}gap".length(), e.getStartIndex());
				Assert.assertEquals("prefix${content0}gap${content1}".length(), e.getEndIndex());
				Assert.assertEquals("content1", e.getContent());
			}

			{
				Expression e = expressions.get(2);
				Assert.assertEquals("${ content2 }", e.getExpression());
				Assert.assertEquals("prefix${content0}gap${content1}gap".length(), e.getStartIndex());
				Assert.assertEquals("prefix${content0}gap${content1}gap${ content2 }".length(), e.getEndIndex());
				Assert.assertEquals("content2", e.getContent());
			}
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix\\${content0}");

			Assert.assertEquals(0, expressions.size());
		}

		{
			List<Expression> expressions = expressionResolver.resolve("prefix${content\\}0}");

			Assert.assertEquals(1, expressions.size());

			Expression e = expressions.get(0);
			Assert.assertEquals("content}0", e.getContent());
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
			String source = "prefix${content}";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("prefixa", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "${content}suffix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("asuffix", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "prefix${content}suffix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("prefixasuffix", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "${ content }";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("a", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "prefix${content0}gap${content1}gap${ content2 }sufix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a", "b", "c");

			Assert.assertEquals("prefixagapbgapcsufix", expressionResolver.evaluate(source, expressions, values, ""));
		}

		{
			String source = "pr\\e\\${fix${content}suffix";
			List<Expression> expressions = expressionResolver.resolve(source);

			List<?> values = Arrays.asList("a");

			Assert.assertEquals("pr\\e${fixasuffix", expressionResolver.evaluate(source, expressions, values, ""));
		}
	}

	@Test
	public void unescapeTest()
	{
		ExpressionResolver expressionResolver = new ExpressionResolver();

		{
			String e = expressionResolver.unescape("prefix\\${content0}");

			Assert.assertEquals("prefix${content0}", e);
		}

		{
			String e = expressionResolver.unescape("prefix${cont\\ent\\}0}");

			Assert.assertEquals("prefix${cont\\ent\\}0}", e);
		}

		{
			String e = expressionResolver.unescape("pre\\afi\\x");

			Assert.assertEquals("pre\\afi\\x", e);
		}
	}

	@Test
	public void extractTest()
	{
		ExpressionResolver expressionResolver = new ExpressionResolver();

		{
			String template = "${value}";
			String value = "@@";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(1, values.size());
			Assert.assertEquals("@@", values.get(0));
		}

		{
			String template = "abc${value}";
			String value = "abc@@";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(1, values.size());
			Assert.assertEquals("@@", values.get(0));
		}

		{
			String template = "${value}def";
			String value = "@@def";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(1, values.size());
			Assert.assertEquals("@@", values.get(0));
		}

		{
			String template = "abc${value}def";
			String value = "abc@@def";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(1, values.size());
			Assert.assertEquals("@@", values.get(0));
		}

		{
			String template = "abc${value1}def${value2}";
			String value = "abc@@def!!";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(2, values.size());
			Assert.assertEquals("@@", values.get(0));
			Assert.assertEquals("!!", values.get(1));
		}

		{
			String template = "abc${value1}def${value2}ghi";
			String value = "abc@@def!!ghi";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(2, values.size());
			Assert.assertEquals("@@", values.get(0));
			Assert.assertEquals("!!", values.get(1));
		}

		{
			String template = "abc${value1}def${value2}ghi";
			String value = "abc@@def!!ggg";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(2, values.size());
			Assert.assertEquals("@@", values.get(0));
			Assert.assertEquals("!!ggg", values.get(1));
		}

		{
			String template = "abc${value1}def${value2}ghi";
			String value = "abcdef";

			List<Expression> expressions = expressionResolver.resolve(template);

			List<String> values = expressionResolver.extract(template, expressions, value);

			Assert.assertEquals(1, values.size());
			Assert.assertEquals("", values.get(0));
		}
	}
}
