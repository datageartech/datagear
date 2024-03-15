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

package org.datagear.persistence.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.meta.Table;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceTestSupport;
import org.datagear.persistence.Row;
import org.datagear.util.sqlvalidator.InvalidPatternSqlValidator;
import org.junit.Test;

/**
 * {@linkplain DefaultPersistenceManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPersistenceManagerTest extends PersistenceTestSupport
{
	private DefaultPersistenceManager defaultPersistenceManager;

	public DefaultPersistenceManagerTest()
	{
		super();
		this.defaultPersistenceManager = new DefaultPersistenceManager(dialectSource);

		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put(InvalidPatternSqlValidator.DEFAULT_PATTERN_KEY,
				InvalidPatternSqlValidator.toKeywordPattern("INSERT", "UPDATE", "DELETE", "TRUNCATE", "CREATE", "ALTER",
						"DROP"));
		InvalidPatternSqlValidator validator = new InvalidPatternSqlValidator(patterns);

		this.defaultPersistenceManager.setQuerySqlValidator(validator);
	}

	@Test
	public void insertTest() throws Exception
	{
		Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");

		int id = 999999999;
		String name = "NAME-FOR-TEST";
		String INTRODUCTION = "INTRODUCTION-for-test";

		Row row = new Row();
		row.put("ID", id);
		row.put("NAME", name);
		row.put("HEAD_IMG", "hex:0x09");
		row.put("INTRODUCTION", INTRODUCTION);

		try
		{
			this.defaultPersistenceManager.delete(connection, table, row);

			this.defaultPersistenceManager.insert(connection, null, table, row, new ConversionSqlParamValueMapper());

			Row actual = this.defaultPersistenceManager.get(connection, null, table, row, null,
					new DefaultLOBRowMapper());

			assertEquals(id, ((Number) actual.get("ID")).intValue());
			assertEquals(name, actual.get("NAME"));
			assertArrayEquals(new byte[] { 0x09 }, (byte[]) actual.get("HEAD_IMG"));
			assertEquals(INTRODUCTION, actual.get("INTRODUCTION"));
		}
		finally
		{
			this.defaultPersistenceManager.delete(connection, table, row);
		}
	}

	@Test
	public void pagingQueryTest()
	{
		Table table = this.genericDBMetaResolver.getTable(this.connection, "T_ACCOUNT");

		PagingData<Row> pagingData = this.defaultPersistenceManager.pagingQuery(connection, table,
				new PagingQuery(1, 1));
		List<Row> rows = pagingData.getItems();

		assertTrue(rows.size() <= 1);
	}
}
