/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SqlReplacerTest
{
	@Test
	public void replaceTest()
	{
		SqlReplacer sqlReplacer = new SqlReplacer();
		
		{
			String sql = "SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = '3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"\" FROM TABLE WHERE ID = ''", actual);
		}

		{
			String sql = "SELECT NAME, \"VA\"\"LUE\" FROM TABLE WHERE ID = '3''3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"\" FROM TABLE WHERE ID = ''", actual);
		}
	}

	@Test
	public void replaceTest_replaceSqlString()
	{
		SqlReplacer sqlReplacer = new SqlReplacer();
		sqlReplacer.setReplaceQuoteIdentifier(false);
		
		{
			String sql = "SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = '3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = ''", actual);
		}

		{
			String sql = "SELECT NAME, \"VA\"\"LUE\" FROM TABLE WHERE ID = '3''3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"VA\"\"LUE\" FROM TABLE WHERE ID = ''", actual);
		}
	}

	@Test
	public void replaceTest_replaceQuoteIdentifier()
	{
		SqlReplacer sqlReplacer = new SqlReplacer();
		sqlReplacer.setReplaceSqlString(false);
		
		{
			String sql = "SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = '3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"\" FROM TABLE WHERE ID = '3'", actual);
		}

		{
			String sql = "SELECT NAME, \"VA\"\"LUE\" FROM TABLE WHERE ID = '3''3'";
			String actual = sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"\" FROM TABLE WHERE ID = '3''3'", actual);
		}
	}
}
