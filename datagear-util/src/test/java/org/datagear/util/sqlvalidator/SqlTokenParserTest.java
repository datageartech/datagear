/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * {@linkplain SqlTokenParser}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTokenParserTest
{
	private SqlTokenParser sqlTokenParser = new SqlTokenParser();

	@Test
	public void parseTest()
	{
		{
			String sql = "SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = '3'";
			List<SqlToken> tokens = this.sqlTokenParser.parse(sql, "\"");

			assertEquals(10, tokens.size());

			{
				SqlToken token = tokens.get(0);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("SELECT", token.getValue());
			}
			{
				SqlToken token = tokens.get(1);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("NAME", token.getValue());
			}
			{
				SqlToken token = tokens.get(2);
				assertEquals(SqlToken.TYPE_COMMA, token.getType());
				assertEquals(",", token.getValue());
			}
			{
				SqlToken token = tokens.get(3);
				assertEquals(SqlToken.TYPE_QUOTE_IDENTIFIER, token.getType());
				assertEquals("\"VALUE\"", token.getValue());
			}
			{
				SqlToken token = tokens.get(4);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("FROM", token.getValue());
			}
			{
				SqlToken token = tokens.get(5);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("TABLE", token.getValue());
			}
			{
				SqlToken token = tokens.get(6);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("WHERE", token.getValue());
			}
			{
				SqlToken token = tokens.get(7);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("ID", token.getValue());
			}
			{
				SqlToken token = tokens.get(8);
				assertEquals(SqlToken.TYPE_OTHER, token.getType());
				assertEquals("=", token.getValue());
			}
			{
				SqlToken token = tokens.get(9);
				assertEquals(SqlToken.TYPE_STRING, token.getType());
				assertEquals("'3'", token.getValue());
			}
		}
	}
}
