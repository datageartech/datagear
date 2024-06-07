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

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.util.JdbcUtil;
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
	@Test
	public void getResultTest() throws Exception
	{
		Connection cn = null;

		long recordId = 999999999;
		String recordName = SqlDataSet.class.getSimpleName();

		try
		{
			cn = getConnection();
			SimpleConnectionFactory connectionFactory = new SimpleConnectionFactory(cn, false);

			{
				String insertSql = "INSERT INTO T_ACCOUNT(ID, NAME) VALUES(" + recordId + ", '" + recordName + "')";
				Statement st = null;

				try
				{
					st = cn.createStatement();
					st.executeUpdate(insertSql);
				}
				finally
				{
					JdbcUtil.closeStatement(st);
				}
			}

			String sql = "SELECT ID, NAME FROM T_ACCOUNT <#if id??>WHERE ID = ${id} AND NAME != '${name}'</#if>";

			List<DataSetField> dataSetFields = Arrays.asList(
					new DataSetField("ID", DataSetField.DataType.INTEGER),
					new DataSetField("NAME", DataSetField.DataType.STRING));

			List<DataSetParam> dataSetParams = Arrays.asList(new DataSetParam("id", DataSetParam.DataType.STRING, true),
					new DataSetParam("name", DataSetParam.DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", dataSetFields, connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlValidator(createSqlValidator());

			{
				Map<String, Object> dataSetParamValues = new HashMap<>();
				dataSetParamValues.put("id", Long.toString(recordId));
				dataSetParamValues.put("name", "name-for-test");

				DataSetResult dataSetResult = sqlDataSet.getResult(DataSetQuery.valueOf(dataSetParamValues));

				@SuppressWarnings("unchecked")
				List<Map<String, ?>> datas = (List<Map<String, ?>>) dataSetResult.getData();

				Assert.assertEquals(1, datas.size());

				{
					Map<String, ?> row = datas.get(0);

					Assert.assertEquals(2, row.size());
					Assert.assertEquals(Long.toString(recordId), row.get("ID").toString());
					Assert.assertEquals(recordName, row.get("NAME"));
				}
			}
		}
		finally
		{
			{
				String insertSql = "DELETE FROM T_ACCOUNT WHERE ID=" + recordId;
				Statement st = null;

				try
				{
					st = cn.createStatement();
					st.executeUpdate(insertSql);
				}
				finally
				{
					JdbcUtil.closeStatement(st);
				}
			}

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
			assertTrue(templateResult.contains(" NAME='" + nameEscape + "'"));
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
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
