/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.datagear.web.util.SqlScriptParser.SqlStatement;
import org.junit.Test;

/**
 * {@linkplain SqlScriptParser}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlScriptParserTest
{
	@Test
	public void parseTestDefaultDelimiterSingle() throws IOException
	{
		String script = "select * from t_order;";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(1, sqlStatements.size());

		SqlStatement sqlStatement = sqlStatements.get(0);

		assertEquals("select * from t_order", sqlStatement.getSql());
		assertEquals(0, sqlStatement.getStartRow());
		assertEquals(0, sqlStatement.getStartColumn());
		assertEquals(0, sqlStatement.getEndRow());
		assertEquals(script.length(), sqlStatement.getEndColumn());
	}

	@Test
	public void parseTestDefaultDelimiterSingleSpace() throws IOException
	{
		String script = "  select * from t_order  ;";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(1, sqlStatements.size());

		SqlStatement sqlStatement = sqlStatements.get(0);

		assertEquals("select * from t_order", sqlStatement.getSql());
		assertEquals(0, sqlStatement.getStartRow());
		assertEquals(0, sqlStatement.getStartColumn());
		assertEquals(0, sqlStatement.getEndRow());
		assertEquals(script.length(), sqlStatement.getEndColumn());
	}

	@Test
	public void parseTestDefaultDelimiterSingleLine() throws IOException
	{
		String script = "--start comment" + SqlScriptParser.LINE_SEPARATOR + "select * from"
				+ SqlScriptParser.LINE_SEPARATOR + "-- center comment" + SqlScriptParser.LINE_SEPARATOR + " t_order;  "
				+ SqlScriptParser.LINE_SEPARATOR + "//end comment";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(1, sqlStatements.size());

		SqlStatement sqlStatement = sqlStatements.get(0);

		assertEquals("select * from" + SqlScriptParser.LINE_SEPARATOR + SqlScriptParser.LINE_SEPARATOR + " t_order",
				sqlStatement.getSql());
		assertEquals(1, sqlStatement.getStartRow());
		assertEquals(0, sqlStatement.getStartColumn());
		assertEquals(3, sqlStatement.getEndRow());
		assertEquals(9, sqlStatement.getEndColumn());
	}

	@Test
	public void parseTestDefaultDelimiterMultiple() throws IOException
	{
		String script = "select * from t_order;update t_product set name='5';delete from t_user;";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(3, sqlStatements.size());

		{
			SqlStatement sqlStatement = sqlStatements.get(0);

			assertEquals("select * from t_order", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals("select * from t_order;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(1);

			assertEquals("update t_product set name='5'", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals("select * from t_order;".length(), sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals("select * from t_order;update t_product set name='5';".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(2);

			assertEquals("delete from t_user", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals("select * from t_order;update t_product set name='5';".length(),
					sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals(script.length(), sqlStatement.getEndColumn());
		}
	}

	@Test
	public void parseTestDefaultDelimiterMultipleSpace() throws IOException
	{
		String script = "  \t  select * from t_order ; \t update t_product set name='5' ; \t delete from t_user; \t \t";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(3, sqlStatements.size());

		{
			SqlStatement sqlStatement = sqlStatements.get(0);

			assertEquals("select * from t_order", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals("  \t  select * from t_order ;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(1);

			assertEquals("update t_product set name='5'", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals("  \t  select * from t_order ;".length(), sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals("  \t  select * from t_order ; \t update t_product set name='5' ;".length(),
					sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(2);

			assertEquals("delete from t_user", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals("  \t  select * from t_order ; \t update t_product set name='5' ;".length(),
					sqlStatement.getStartColumn());
			assertEquals(0, sqlStatement.getEndRow());
			assertEquals(
					"  \t  select * from t_order ; \t update t_product set name='5' ; \t delete from t_user;".length(),
					sqlStatement.getEndColumn());
		}
	}

	@Test
	public void parseTestDefaultDelimiterMultipleSpaceLine() throws IOException
	{
		String script = "select \t *" + SqlScriptParser.LINE_SEPARATOR + " from t_order;"
				+ SqlScriptParser.LINE_SEPARATOR + "--update " + SqlScriptParser.LINE_SEPARATOR
				+ " \t update t_product set name='5' ;" + SqlScriptParser.LINE_SEPARATOR + "--delete"
				+ SqlScriptParser.LINE_SEPARATOR + " \t delete from t_user; \t \t";

		SqlScriptParser parser = new SqlScriptParser(toStringReader(script));

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(3, sqlStatements.size());

		{
			SqlStatement sqlStatement = sqlStatements.get(0);

			assertEquals("select \t *" + SqlScriptParser.LINE_SEPARATOR + " from t_order", sqlStatement.getSql());
			assertEquals(0, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(1, sqlStatement.getEndRow());
			assertEquals(" from t_order;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(1);

			assertEquals("update t_product set name='5'", sqlStatement.getSql());
			assertEquals(3, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(3, sqlStatement.getEndRow());
			assertEquals(" \t update t_product set name='5' ;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(2);

			assertEquals("delete from t_user", sqlStatement.getSql());
			assertEquals(5, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(5, sqlStatement.getEndRow());
			assertEquals(" \t delete from t_user;".length(), sqlStatement.getEndColumn());
		}
	}

	@Test
	public void parseTestScriptFile() throws IOException
	{
		InputStream inputStream = SqlScriptParserTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/web/util/SqlScriptParserTest.sql");

		Reader reader = new InputStreamReader(inputStream, "UTF-8");

		SqlScriptParser parser = new SqlScriptParser(reader);

		List<SqlStatement> sqlStatements = parser.parse();

		assertEquals(4, sqlStatements.size());

		{
			SqlStatement sqlStatement = sqlStatements.get(0);

			assertEquals("select" + SqlScriptParser.LINE_SEPARATOR + "\t*" + SqlScriptParser.LINE_SEPARATOR + "from"
					+ SqlScriptParser.LINE_SEPARATOR + "\tt_order", sqlStatement.getSql());
			assertEquals(2, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(6, sqlStatement.getEndRow());
			assertEquals(1, sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(1);

			assertEquals("select * from t_product", sqlStatement.getSql());
			assertEquals(10, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(10, sqlStatement.getEndRow());
			assertEquals("select * from t_product;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(2);

			assertEquals("delete" + SqlScriptParser.LINE_SEPARATOR + SqlScriptParser.LINE_SEPARATOR + "from"
					+ SqlScriptParser.LINE_SEPARATOR + "\tt_user", sqlStatement.getSql());
			assertEquals(10, sqlStatement.getStartRow());
			assertEquals("select * from t_product;".length(), sqlStatement.getStartColumn());
			assertEquals(13, sqlStatement.getEndRow());
			assertEquals("	t_user;".length(), sqlStatement.getEndColumn());
		}

		{
			SqlStatement sqlStatement = sqlStatements.get(3);

			assertEquals("update \r\n\r\n\r\n\r\nt_user \r\n\r\n\r\nset name = '3'", sqlStatement.getSql());
			assertEquals(17, sqlStatement.getStartRow());
			assertEquals(0, sqlStatement.getStartColumn());
			assertEquals(24, sqlStatement.getEndRow());
			assertEquals("set name = '3';".length(), sqlStatement.getEndColumn());
		}
	}

	protected StringReader toStringReader(String s)
	{
		return new StringReader(s);
	}
}
