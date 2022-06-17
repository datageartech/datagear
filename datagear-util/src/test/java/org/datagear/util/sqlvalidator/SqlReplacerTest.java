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
	private SqlReplacer sqlReplacer = new SqlReplacer();

	@Test
	public void replaceTest()
	{
		{
			String sql = "SELECT NAME, \"VALUE\" FROM TABLE WHERE ID = '3'";
			String actual = this.sqlReplacer.replace(sql, "\"");

			assertEquals("SELECT NAME, \"\" FROM TABLE WHERE ID = ''", actual);
		}
	}
}
