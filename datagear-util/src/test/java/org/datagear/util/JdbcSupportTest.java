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

package org.datagear.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.datagear.util.test.DBTestSupport;
import org.junit.Test;


/**
 * {@linkplain JdbcSupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcSupportTest extends DBTestSupport
{
	private JdbcSupport jdbcSupport = new JdbcSupport();

	@SuppressWarnings("deprecation")
	@Test
	public void getColumnValueExtractTest() throws Exception
	{
		Connection cn = null;

		int id = 999123456;
		String name = "JdbcSupportTest";
		long ms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2025-04-24 11:02:00").getTime();
		Date date = new Date(ms);
		Date dateTime = new Date(ms + 1000 * 60 * 60 * 60 * 72);
		Time time = new Time(ms + 1000 * 60 * 60);
		Timestamp timestamp = new Timestamp(ms + 1000 * 60 * 2);
		byte[] blob = new byte[] { 33, 55, 66, 77, 88, 99, 12, 16, 32, 51, 63 };
		String clob = "aaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccc";

		try
		{
			cn = getConnection();

			{
				Sql sql = Sql.valueOf(
						"INSERT INTO T_DATA_IMPORT (ID, NAME, COL_DATE, COL_DATETIME, COL_TIME, COL_TIMESTAMP, COL_BLOB, COL_CLOB) "
								+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

				sql.param(jdbcSupport.toSqlParamValue(id));
				sql.param(jdbcSupport.toSqlParamValue(name));
				sql.param(jdbcSupport.toSqlParamValue(date));
				sql.param(jdbcSupport.toSqlParamValue(dateTime));
				sql.param(jdbcSupport.toSqlParamValue(time));
				sql.param(jdbcSupport.toSqlParamValue(timestamp));
				sql.param(jdbcSupport.toSqlParamValue(blob));
				sql.param(jdbcSupport.toSqlParamValue(clob));

				jdbcSupport.executeUpdate(cn, sql);
			}

			{
				Sql sql = Sql.valueOf(
						"SELECT ID, NAME, COL_DATE, COL_DATETIME, COL_TIME, COL_TIMESTAMP, COL_BLOB, COL_CLOB FROM T_DATA_IMPORT WHERE ID = ?");
				sql.param(jdbcSupport.toSqlParamValue(id));

				QueryResultSet re = jdbcSupport.executeQuery(cn, sql, ResultSet.TYPE_FORWARD_ONLY);
				ResultSet rs = re.getResultSet();
				rs.next();

				{
					Number v = (Number) jdbcSupport.getColumnValueExtract(cn, rs, "ID", Types.INTEGER);
					assertEquals(id, v.intValue());
				}

				{
					String v = (String) jdbcSupport.getColumnValueExtract(cn, rs, "NAME", Types.VARCHAR);
					assertEquals(name, v);
				}

				{
					java.sql.Date v = (java.sql.Date) jdbcSupport.getColumnValueExtract(cn, rs, "COL_DATE", Types.DATE);
					assertEquals(date.getYear(), v.getYear());
					assertEquals(date.getMonth(), v.getMonth());
					assertEquals(date.getDay(), v.getDay());
				}

				{
					java.sql.Date v = (java.sql.Date) jdbcSupport.getColumnValueExtract(cn, rs, "COL_DATETIME",
							Types.DATE);
					assertEquals(dateTime.getYear(), v.getYear());
					assertEquals(dateTime.getMonth(), v.getMonth());
					assertEquals(dateTime.getDay(), v.getDay());
				}

				{
					Time v = (Time) jdbcSupport.getColumnValueExtract(cn, rs, "COL_TIME", Types.TIME);
					assertEquals(time.getHours(), v.getHours());
					assertEquals(time.getMinutes(), v.getMinutes());
					assertEquals(time.getSeconds(), v.getSeconds());
				}

				{
					Timestamp v = (Timestamp) jdbcSupport.getColumnValueExtract(cn, rs, "COL_TIMESTAMP",
							Types.TIMESTAMP);
					assertEquals(timestamp.getYear(), v.getYear());
					assertEquals(timestamp.getMonth(), v.getMonth());
					assertEquals(timestamp.getDay(), v.getDay());
					assertEquals(timestamp.getHours(), v.getHours());
					assertEquals(timestamp.getMinutes(), v.getMinutes());
					assertEquals(timestamp.getSeconds(), v.getSeconds());
				}

				{
					{
						byte[] v = (byte[]) jdbcSupport.getColumnValueExtract(cn, rs, "COL_BLOB", Types.BLOB);
						assertEquals(blob.length, v.length);
						for (int i = 0; i < v.length; i++)
						{
							assertEquals(blob[i], v[i]);
						}
					}

					{
						byte[] v = (byte[]) jdbcSupport.getColumnValueExtract(cn, rs, "COL_BLOB", Types.BINARY);
						assertEquals(blob.length, v.length);
						for (int i = 0; i < v.length; i++)
						{
							assertEquals(blob[i], v[i]);
						}
					}

					{
						byte[] v = (byte[]) jdbcSupport.getColumnValueExtract(cn, rs, "COL_BLOB", Types.LONGVARBINARY);
						assertEquals(blob.length, v.length);
						for (int i = 0; i < v.length; i++)
						{
							assertEquals(blob[i], v[i]);
						}
					}

					{
						byte[] v = (byte[]) jdbcSupport.getColumnValueExtract(cn, rs, "COL_BLOB", Types.VARBINARY);
						assertEquals(blob.length, v.length);
						for (int i = 0; i < v.length; i++)
						{
							assertEquals(blob[i], v[i]);
						}
					}
				}

				{

					{
						String v = (String) jdbcSupport.getColumnValueExtract(cn, rs, "COL_CLOB", Types.CLOB);
						assertEquals(clob, v);
					}

					{
						String v = (String) jdbcSupport.getColumnValueExtract(cn, rs, "COL_CLOB", Types.LONGNVARCHAR);
						assertEquals(clob, v);
					}

					{
						String v = (String) jdbcSupport.getColumnValueExtract(cn, rs, "COL_CLOB", Types.LONGVARCHAR);
						assertEquals(clob, v);
					}

					{
						String v = (String) jdbcSupport.getColumnValueExtract(cn, rs, "COL_CLOB", Types.NCLOB);
						assertEquals(clob, v);
					}
				}
			}
		}
		finally
		{
			Sql sql = Sql.valueOf("DELETE FROM T_DATA_IMPORT WHERE ID = ?");
			sql.param(jdbcSupport.toSqlParamValue(id));
			jdbcSupport.executeUpdate(cn, sql);

			IOUtil.close(cn);
		}
	};

	@Test
	public void toSqlParamValueTest()
	{
		{
			Object v = null;
			SqlParamValue spv = jdbcSupport.toSqlParamValue(v);
			assertEquals(Types.NULL, spv.getType());
			assertNull(spv.getValue());
		}

		{
			String v = "a";
			SqlParamValue spv = jdbcSupport.toSqlParamValue(v);
			assertEquals(Types.VARCHAR, spv.getType());
			assertEquals("a", spv.getValue());
		}
	}

	@Test
	public void toSqlParamValuesTest()
	{
		{
			List<Object> vs = Arrays.asList("aaa", 3, true, null);
			List<SqlParamValue> spvs = jdbcSupport.toSqlParamValues(vs);

			assertEquals(Types.VARCHAR, spvs.get(0).getType());
			assertEquals("aaa", spvs.get(0).getValue());

			assertEquals(Types.INTEGER, spvs.get(1).getType());
			assertEquals(3, spvs.get(1).getValue());

			assertEquals(Types.BOOLEAN, spvs.get(2).getType());
			assertEquals(true, spvs.get(2).getValue());

			assertEquals(Types.NULL, spvs.get(3).getType());
			assertNull(spvs.get(3).getValue());
		}

		{
			Object[] vs = new Object[] { "aaa", 3, true, null };
			List<SqlParamValue> spvs = jdbcSupport.toSqlParamValues(vs);

			assertEquals(Types.VARCHAR, spvs.get(0).getType());
			assertEquals("aaa", spvs.get(0).getValue());

			assertEquals(Types.INTEGER, spvs.get(1).getType());
			assertEquals(3, spvs.get(1).getValue());

			assertEquals(Types.BOOLEAN, spvs.get(2).getType());
			assertEquals(true, spvs.get(2).getValue());

			assertEquals(Types.NULL, spvs.get(3).getType());
			assertNull(spvs.get(3).getValue());
		}
	}

	@Test
	public void getJdbcTypeTest()
	{
		{
			Object v = null;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.NULL, type);
		}

		{
			String v = "a";
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.VARCHAR, type);
		}

		{
			boolean v = true;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BOOLEAN, type);
		}
		{
			Boolean v = new Boolean(false);
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BOOLEAN, type);
		}

		{
			int v = 3;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.INTEGER, type);
		}
		{
			Integer v = new Integer(3);
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.INTEGER, type);
		}

		{
			long v = 3L;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BIGINT, type);
		}
		{
			Long v = new Long(3L);
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BIGINT, type);
		}

		{
			float v = 3.2F;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.FLOAT, type);
		}
		{
			Float v = new Float(3.2F);
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.FLOAT, type);
		}

		{
			double v = 3.26D;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.DOUBLE, type);
		}
		{
			Double v = new Double(3.26D);
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.DOUBLE, type);
		}

		{
			byte v = 3;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.TINYINT, type);
		}
		{
			Byte v = new Byte(Integer.valueOf(3).byteValue());
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.TINYINT, type);
		}

		{
			short v = 3;
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.SMALLINT, type);
		}
		{
			Short v = new Short(Integer.valueOf(3).shortValue());
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.SMALLINT, type);
		}

		{
			char v = 'a';
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.CHAR, type);
		}
		{
			Character v = new Character('a');
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.CHAR, type);
		}

		{
			BigDecimal v = new BigDecimal("3.25");
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.NUMERIC, type);
		}
		{
			BigInteger v = new BigInteger("3");
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.NUMERIC, type);
		}

		{
			java.sql.Date v = new java.sql.Date(System.currentTimeMillis());
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.DATE, type);
		}

		{
			java.sql.Time v = new java.sql.Time(System.currentTimeMillis());
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.TIME, type);
		}

		{
			java.sql.Timestamp v = new java.sql.Timestamp(System.currentTimeMillis());
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.TIMESTAMP, type);
		}

		{
			java.util.Date v = new java.util.Date();
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.DATE, type);
		}

		{
			byte[] v = new byte[] { 3, 4, 5 };
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BINARY, type);
		}
		{
			Byte[] v = new Byte[] { 3, 4, 5 };
			int type = jdbcSupport.getJdbcType(v);
			assertEquals(Types.BINARY, type);
		}
	}
}
