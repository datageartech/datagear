/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.SqlBuilder;
import org.datagear.util.JdbcUtil;
import org.datagear.util.JdbcUtil.QueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象DAO支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataAccessObject
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataAccessObject.class);

	public AbstractDataAccessObject()
	{
		super();
	}

	/**
	 * 获取指定类型的列值。
	 * 
	 * @param rs
	 * @param row
	 * @param columnIndex
	 * @param targetType
	 * @return
	 */
	protected Object getColumnValue(ResultSet rs, long row, int columnIndex, Class<?> targetType)
	{
		try
		{
			return JdbcUtil.getColumnValue(rs, row, columnIndex, targetType);
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
	}

	/**
	 * 根据列号查找列名称。
	 * <p>
	 * 拷贝自org.springframework.jdbc.support.JdbcUtils.lookupColumnName
	 * </p>
	 * 
	 * @param rsMeta
	 * @param columnIndex
	 * @return
	 */
	protected String lookupColumnName(ResultSetMetaData rsMeta, int columnIndex)
	{
		try
		{
			String name = rsMeta.getColumnLabel(columnIndex);
			if (name == null || name.isEmpty())
				name = rsMeta.getColumnName(columnIndex);

			return name;
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
	}

	/**
	 * 执行sql更新操作。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 */
	protected int executeUpdate(Connection cn, SqlBuilder sql)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(sql.toString());

		PreparedStatement pst = null;

		try
		{
			pst = cn.prepareStatement(sql.getSqlString());
			sql.setParamValues(cn, pst);

			return pst.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			JdbcUtil.closeStatement(pst);
		}
	}

	/**
	 * 执行sql更新操作，并返回自动生成列值。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 */
	protected GeneratedKeysUpdateResult executeUpdateWithGeneratedKeys(Connection cn, SqlBuilder sql,
			String[] generatedKeyColumnNames)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(sql.toString());

		PreparedStatement pst = null;

		try
		{
			pst = cn.prepareStatement(sql.getSqlString(), generatedKeyColumnNames);
			sql.setParamValues(cn, pst);

			int updateCount = pst.executeUpdate();
			ResultSet generatedKeys = pst.getGeneratedKeys();

			return new GeneratedKeysUpdateResult(pst, updateCount, generatedKeys);
		}
		catch (SQLException e)
		{
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}
	}

	/**
	 * 执行列表查询。
	 * 
	 * @param cn
	 * @param query
	 * @param rowMapper
	 * @return
	 */
	protected List<Object> executeListQuery(Connection cn, SqlBuilder query, RowMapper<Object> rowMapper)
	{
		return executeListQuery(cn, query, rowMapper, 1, -1);
	}

	/**
	 * 执行列表查询。
	 * 
	 * @param cn
	 * @param query
	 * @param rowMapper
	 * @param startRow
	 *            起始行号，以1开头
	 * @param count
	 *            读取记录数，如果{@code <0}，表示读取全部
	 * @return
	 */
	protected List<Object> executeListQuery(Connection cn, SqlBuilder query, RowMapper<Object> rowMapper, int startRow,
			int count)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(query.toString());

		if (startRow < 1)
			startRow = 1;

		List<Object> resultList = new ArrayList<Object>();

		Statement st = null;
		ResultSet rs = null;

		try
		{
			QueryResultSet queryResultSet = JdbcUtil.executeQuery(cn, query.getSqlString(), query.getArgTypes(),
					query.getArgs());

			st = queryResultSet.getStatement();
			rs = queryResultSet.getResultSet();

			if (count >= 0 && startRow > 1)
				JdbcUtil.moveToBeforeRow(rs, startRow - 1);

			int endRow = (count >= 0 ? startRow + count : -1);

			int row = startRow;
			while (rs.next())
			{
				if (endRow >= 0 && row >= endRow)
					break;

				Object rowObj = rowMapper.mapRow(rs, row);

				resultList.add(rowObj);

				row++;
			}

			return resultList;
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 执行数目查询。
	 * 
	 * @param cn
	 * @param query
	 * @return
	 */
	protected long executeCountQuery(Connection cn, SqlBuilder query)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(query.toString());

		Statement st = null;
		ResultSet rs = null;

		try
		{
			long count = 0;

			QueryResultSet queryResultSet = JdbcUtil.executeQuery(cn, query.getSqlString(), query.getArgTypes(),
					query.getArgs());

			st = queryResultSet.getStatement();
			rs = queryResultSet.getResultSet();

			if (rs.next())
				count = rs.getInt(1);

			return count;
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 带有自动生成信息的sql更新结果。
	 * <p>
	 * 不再使用后要调用它的{@linkplain #close()}方法。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class GeneratedKeysUpdateResult
	{
		private Statement statement;

		private int updateCount;

		private ResultSet generatedKeys;

		public GeneratedKeysUpdateResult()
		{
			super();
		}

		public GeneratedKeysUpdateResult(Statement statement, int updateCount, ResultSet generatedKeys)
		{
			super();
			this.statement = statement;
			this.updateCount = updateCount;
			this.generatedKeys = generatedKeys;
		}

		public Statement getStatement()
		{
			return statement;
		}

		public void setStatement(Statement statement)
		{
			this.statement = statement;
		}

		public int getUpdateCount()
		{
			return updateCount;
		}

		public void setUpdateCount(int updateCount)
		{
			this.updateCount = updateCount;
		}

		public ResultSet getGeneratedKeys()
		{
			return generatedKeys;
		}

		public void setGeneratedKeys(ResultSet generatedKeys)
		{
			this.generatedKeys = generatedKeys;
		}

		public void close()
		{
			JdbcUtil.closeResultSet(generatedKeys);
			JdbcUtil.closeStatement(statement);
		}
	}

	/**
	 * 行映射器。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	public static interface RowMapper<T>
	{
		/**
		 * 将{@linkplain ResultSet}的指定行数据映射为对象。
		 * 
		 * @param rs
		 * @param row
		 *            行号，以1开始
		 * @return
		 */
		T mapRow(ResultSet rs, int row);
	}
}
