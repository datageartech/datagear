/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
