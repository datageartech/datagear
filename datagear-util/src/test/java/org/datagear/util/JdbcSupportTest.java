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
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


/**
 * {@linkplain JdbcSupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcSupportTest
{
	private JdbcSupport jdbcSupport = new JdbcSupport();

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
