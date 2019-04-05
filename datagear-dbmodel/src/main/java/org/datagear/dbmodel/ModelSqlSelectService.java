/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.JdbcUtil;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.persistence.support.AbstractModelDataAccessObject;
import org.datagear.persistence.support.UUID;

/**
 * 基于查询SQL的模型数据查询服务类。
 * 
 * @author datagear@163.com
 *
 */
public class ModelSqlSelectService extends AbstractModelDataAccessObject
{
	public ModelSqlSelectService()
	{
		super();
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @param databaseModelResolver
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, int startRow, int fetchSize,
			DatabaseModelResolver databaseModelResolver) throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;
		try
		{
			st = cn.createStatement();
			rs = st.executeQuery(sql);

			Model model = databaseModelResolver.resolve(cn, rs, UUID.gen());

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;
		try
		{
			st = cn.createStatement();
			rs = st.executeQuery(sql);

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param rs
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, ResultSet rs, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		ModelSqlResult modelSqlResult = new ModelSqlResult();
		modelSqlResult.setSql(sql);
		modelSqlResult.setModel(model);
		modelSqlResult.setStartRow(startRow);
		modelSqlResult.setFetchSize(fetchSize);

		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		Property[] properties = model.getProperties();

		if (properties == null || properties.length != columnCount)
			throw new IllegalArgumentException("ResultSet not match Model");

		List<Object> datas = new ArrayList<Object>();

		rs.absolute(startRow - 1);

		for (int row = startRow; row < startRow + fetchSize; row++)
		{
			if (!rs.next())
				break;

			Object data = model.newInstance();

			for (int j = 1; j <= columnCount; j++)
			{
				Property property = properties[j - 1];

				if (!MU.isConcretePrimitiveProperty(property))
					throw new IllegalArgumentException(
							"The " + (j - 1) + "-th property must be concrete and primitive");

				Object columnValue = toPropertyValue(cn, rs, row, j, model, property,
						PropertyModel.valueOf(property, 0));

				property.set(data, columnValue);
			}

			datas.add(data);
		}

		modelSqlResult.setDatas(datas);

		return modelSqlResult;
	}

	/**
	 * 模型查询结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ModelSqlResult
	{
		private String sql;

		private Model model;

		private int startRow;

		private int fetchSize;

		private List<Object> datas;

		public ModelSqlResult()
		{
			super();
		}

		public ModelSqlResult(String sql, Model model, int startRow, int fetchSize, List<Object> datas)
		{
			super();
			this.sql = sql;
			this.model = model;
			this.startRow = startRow;
			this.fetchSize = fetchSize;
			this.datas = datas;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		public int getStartRow()
		{
			return startRow;
		}

		public void setStartRow(int startRow)
		{
			this.startRow = startRow;
		}

		public int getFetchSize()
		{
			return fetchSize;
		}

		public void setFetchSize(int fetchSize)
		{
			this.fetchSize = fetchSize;
		}

		public boolean hasData()
		{
			return (this.datas != null && !this.datas.isEmpty());
		}

		public List<Object> getDatas()
		{
			return datas;
		}

		public void setDatas(List<Object> datas)
		{
			this.datas = datas;
		}

		public boolean hasMoreData()
		{
			return this.datas != null && this.datas.size() >= this.fetchSize;
		}

		public int getNextStartRow()
		{
			return this.startRow + this.fetchSize;
		}
	}
}
