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

package org.datagear.analysis.support;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.util.JdbcSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.Sql;
import org.datagear.util.resource.SimpleConnectionFactory;
import org.datagear.util.sqlvalidator.InvalidPatternSqlValidator;
import org.datagear.util.sqlvalidator.SqlValidator;
import org.datagear.util.test.DBTestSupport;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain SqlDataSet}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetTest extends DBTestSupport
{
	private JdbcSupport jdbcSupport = new JdbcSupport();

	@Test
	public void getResultTest() throws Exception
	{
		Connection cn = null;

		long id = 992349809;
		String name = "getResultTest";
		byte[] headImg = new byte[] { 33, 55, 66, 77, 88, 99, 12, 16, 32, 51, 63 };
		String introduction = "aaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccc";

		try
		{
			cn = getConnection();
			SimpleConnectionFactory connectionFactory = new SimpleConnectionFactory(cn, false);

			insertAccount(cn, id, name, headImg, introduction);

			String sql = "SELECT ID, NAME, HEAD_IMG, INTRODUCTION FROM T_ACCOUNT <#if id??>WHERE ID = ${id} AND NAME != '${name}'</#if>";

			List<DataSetField> dataSetFields = Arrays.asList(
					new DataSetField("ID", DataSetField.DataType.INTEGER),
					new DataSetField("NAME", DataSetField.DataType.STRING),
					new DataSetField("HEAD_IMG", DataSetField.DataType.STRING),
					new DataSetField("INTRODUCTION", DataSetField.DataType.STRING));

			List<DataSetParam> dataSetParams = Arrays.asList(new DataSetParam("id", DataSetParam.DataType.STRING, true),
					new DataSetParam("name", DataSetParam.DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", dataSetFields, connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlValidator(createSqlValidator());

			{
				Map<String, Object> dataSetParamValues = new HashMap<>();
				dataSetParamValues.put("id", Long.toString(id));
				dataSetParamValues.put("name", "name-for-test");

				DataSetResult dataSetResult = sqlDataSet.getResult(DataSetQuery.valueOf(dataSetParamValues));

				@SuppressWarnings("unchecked")
				List<Map<String, ?>> datas = (List<Map<String, ?>>) dataSetResult.getData();

				Assert.assertEquals(1, datas.size());

				{
					Map<String, ?> row = datas.get(0);

					Assert.assertEquals(4, row.size());
					Assert.assertEquals(Long.toString(id), row.get("ID").toString());
					Assert.assertEquals(name, row.get("NAME"));
					Assert.assertEquals(Base64.getEncoder().encodeToString(headImg), row.get("HEAD_IMG"));
					Assert.assertEquals(introduction, row.get("INTRODUCTION"));
				}
			}
		}
		finally
		{
			deleteAccount(cn, id);
			JdbcUtil.closeConnection(cn);
		}
	}

	@Test
	public void getResultTest_precompile() throws Exception
	{
		Connection cn = null;

		long id = 993699993;
		String name = "getResultTest";
		byte[] headImg = new byte[] { 33, 55, 66, 77, 82, 99, 12, 16, 32, 51, 63 };
		String introduction = "111122223333";

		try
		{
			cn = getConnection();
			SimpleConnectionFactory connectionFactory = new SimpleConnectionFactory(cn, false);

			insertAccount(cn, id, name, headImg, introduction);

			String sql = "SELECT ID, NAME, HEAD_IMG, INTRODUCTION FROM T_ACCOUNT <#if id??>WHERE ID = ${pc(id)} AND NAME != ${pc(name)}</#if>";

			List<DataSetField> dataSetFields = Arrays.asList(new DataSetField("ID", DataSetField.DataType.INTEGER),
					new DataSetField("NAME", DataSetField.DataType.STRING),
					new DataSetField("HEAD_IMG", DataSetField.DataType.STRING),
					new DataSetField("INTRODUCTION", DataSetField.DataType.STRING));

			List<DataSetParam> dataSetParams = Arrays.asList(new DataSetParam("id", DataSetParam.DataType.NUMBER, true),
					new DataSetParam("name", DataSetParam.DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", dataSetFields, connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlValidator(createSqlValidator());

			{
				Map<String, Object> dataSetParamValues = new HashMap<>();
				dataSetParamValues.put("id", id);
				dataSetParamValues.put("name", "name-for-test");

				DataSetResult dataSetResult = sqlDataSet.getResult(DataSetQuery.valueOf(dataSetParamValues));

				@SuppressWarnings("unchecked")
				List<Map<String, ?>> datas = (List<Map<String, ?>>) dataSetResult.getData();

				Assert.assertEquals(1, datas.size());

				{
					Map<String, ?> row = datas.get(0);

					Assert.assertEquals(4, row.size());
					Assert.assertEquals(Long.toString(id), row.get("ID").toString());
					Assert.assertEquals(name, row.get("NAME"));
					Assert.assertEquals(Base64.getEncoder().encodeToString(headImg), row.get("HEAD_IMG"));
					Assert.assertEquals(introduction, row.get("INTRODUCTION"));
				}
			}
		}
		finally
		{
			deleteAccount(cn, id);
			JdbcUtil.closeConnection(cn);
		}
	}

	@Test
	public void resolveResultTest() throws Exception
	{
		Connection cn = null;

		long id = 993699995;
		String name = "resolveResultTest";
		byte[] headImg = new byte[] { 25, 36, 26, 67, 86, 39, 42, 66, 32, 57, 62 };
		String introduction = "111111111111111112222222222222222222222222333333333333333333333333";

		try
		{
			cn = getConnection();
			SimpleConnectionFactory connectionFactory = new SimpleConnectionFactory(cn, false);

			insertAccount(cn, id, name, headImg, introduction);

			String sql = "SELECT ID, NAME, HEAD_IMG, INTRODUCTION FROM T_ACCOUNT WHERE ID = ${id}";

			List<DataSetParam> dataSetParams = Arrays
					.asList(new DataSetParam("id", DataSetParam.DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlValidator(createSqlValidator());

			{
				Map<String, Object> dataSetParamValues = new HashMap<>();
				dataSetParamValues.put("id", Long.toString(id));

				TemplateResolvedDataSetResult rr = sqlDataSet.resolve(DataSetQuery.valueOf(dataSetParamValues));
				String sqlTpl = rr.getTemplateResult();
				List<DataSetField> fields = rr.getFields();
				DataSetResult dataSetResult = rr.getResult();

				{
					Assert.assertEquals(
							"SELECT ID, NAME, HEAD_IMG, INTRODUCTION FROM T_ACCOUNT WHERE ID = " + Long.toString(id),
							sqlTpl);
				}

				{
					Assert.assertEquals(4, fields.size());

					Assert.assertEquals("ID", fields.get(0).getName());
					Assert.assertEquals(DataSetField.DataType.INTEGER, fields.get(0).getType());

					Assert.assertEquals("NAME", fields.get(1).getName());
					Assert.assertEquals(DataSetField.DataType.STRING, fields.get(1).getType());

					Assert.assertEquals("HEAD_IMG", fields.get(2).getName());
					Assert.assertEquals(DataSetField.DataType.STRING, fields.get(2).getType());

					Assert.assertEquals("INTRODUCTION", fields.get(3).getName());
					Assert.assertEquals(DataSetField.DataType.STRING, fields.get(3).getType());
				}

				{
					@SuppressWarnings("unchecked")
					List<Map<String, ?>> datas = (List<Map<String, ?>>) dataSetResult.getData();

					Assert.assertEquals(1, datas.size());

					{
						Map<String, ?> row = datas.get(0);

						Assert.assertEquals(4, row.size());
						Assert.assertEquals(Long.toString(id), row.get("ID").toString());
						Assert.assertEquals(name, row.get("NAME"));
						Assert.assertEquals(Base64.getEncoder().encodeToString(headImg), row.get("HEAD_IMG"));
						Assert.assertEquals(introduction, row.get("INTRODUCTION"));
					}
				}
			}
		}
		finally
		{
			deleteAccount(cn, id);
			JdbcUtil.closeConnection(cn);
		}
	}

	@Test
	public void getResultTest_escape() throws Exception
	{
		String name = "aa---'---";
		String nameEscape = "aa---''---";

		Connection cn = null;

		try
		{
			cn = getConnection();
			SimpleConnectionFactory connectionFactory = new SimpleConnectionFactory(cn, false);

			String sql = "SELECT ID, NAME FROM T_ACCOUNT WHERE NAME='${name}'";

			List<DataSetField> dataSetFields = Arrays.asList(
					new DataSetField("ID", DataSetField.DataType.INTEGER),
					new DataSetField("NAME", DataSetField.DataType.STRING));

			List<DataSetParam> dataSetParams = Arrays
					.asList(new DataSetParam("name", DataSetParam.DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", dataSetFields, connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlValidator(createSqlValidator());

			Map<String, Object> dataSetParamValues = new HashMap<>();
			dataSetParamValues.put("name", name);

			TemplateResolvedDataSetResult result = sqlDataSet.resolve(DataSetQuery.valueOf(dataSetParamValues));
			String templateResult = result.getTemplateResult();
			Assert.assertTrue(templateResult.contains(" NAME='" + nameEscape + "'"));
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	protected void insertAccount(Connection cn, long id, String name, byte[] headImg, String introduction)
			throws Exception
	{
		Sql sql = Sql.valueOf("INSERT INTO T_ACCOUNT(ID, NAME, HEAD_IMG, INTRODUCTION) VALUES(?, ?, ?, ?)");

		sql.param(jdbcSupport.toSqlParamValue(id));
		sql.param(jdbcSupport.toSqlParamValue(name));
		sql.param(jdbcSupport.toSqlParamValue(headImg));
		sql.param(jdbcSupport.toSqlParamValue(introduction));

		jdbcSupport.executeUpdate(cn, sql);
	}

	protected void deleteAccount(Connection cn, long id)
	{
		try
		{
			Sql sql = Sql.valueOf("DELETE FROM T_ACCOUNT WHERE ID=" + id);
			jdbcSupport.executeUpdate(cn, sql);
		}
		catch (Throwable t)
		{
		}
	}

	protected SqlValidator createSqlValidator()
	{
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put(InvalidPatternSqlValidator.DEFAULT_PATTERN_KEY,
				InvalidPatternSqlValidator.toKeywordPattern("INSERT", "UPDATE", "DELETE", "TRUNCATE", "CREATE", "ALTER",
						"DROP"));

		return new InvalidPatternSqlValidator(patterns);
	}
}
