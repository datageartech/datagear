/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataType;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.SimpleConnectionFactory;
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

			List<DataSetProperty> dataSetProperties = Arrays.asList(new DataSetProperty("ID", DataType.INTEGER),
					new DataSetProperty("NAME", DataType.STRING));
			List<DataSetParam> dataSetParams = Arrays.asList(new DataSetParam("id", DataType.STRING, true),
					new DataSetParam("name", DataType.STRING, true));

			SqlDataSet sqlDataSet = new SqlDataSet("1", "1", dataSetProperties, connectionFactory, sql);
			sqlDataSet.setParams(dataSetParams);
			sqlDataSet.setSqlDataSetSqlResolver(new SqlDataSetFmkSqlResolver());

			{
				Map<String, Object> dataSetParamValues = new HashMap<>();
				dataSetParamValues.put("id", Long.toString(recordId));
				dataSetParamValues.put("name", "name-for-test");

				DataSetResult dataSetResult = sqlDataSet.getResult(dataSetParamValues);

				@SuppressWarnings("unchecked")
				List<Map<String, ?>> datas = (List<Map<String, ?>>) dataSetResult.getDatas();

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
}
