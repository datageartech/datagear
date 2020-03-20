/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.meta.TableType;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;

/**
 * SQL查询管理类。
 * <p>
 * 它将SQL查询当作一个虚拟的{@linkplain Table}来处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlSelectManager extends PersistenceSupport
{
	public static final String SELECT_TABLE_NAME = "TableForSqlSelect";

	private DBMetaResolver dbMetaResolver;

	public SqlSelectManager()
	{
		super();
	}

	public SqlSelectManager(DBMetaResolver dbMetaResolver)
	{
		super();
		this.dbMetaResolver = dbMetaResolver;
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	/**
	 * 查询SQL并返回结果。
	 * 
	 * @param cn
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public SqlSelectResult select(Connection cn, String sql, int startRow, int fetchSize) throws SQLException
	{
		return select(cn, sql, startRow, fetchSize, null);
	}

	/**
	 * 查询SQL并返回结果。
	 * 
	 * @param cn
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @param rowMapper
	 *            允许为{@code null}
	 * @return
	 * @throws SQLException
	 */
	public SqlSelectResult select(Connection cn, String sql, int startRow, int fetchSize, RowMapper rowMapper)
			throws SQLException
	{
		QueryResultSet qrs = null;

		Sql sqlo = Sql.valueOf(sql);

		try
		{
			qrs = executeQuery(cn, sqlo, ResultSet.TYPE_FORWARD_ONLY);
			ResultSet rs = qrs.getResultSet();
			Table table = buildTable(cn, rs);

			List<Row> rows = mapToRows(cn, table, rs, startRow, fetchSize, rowMapper);

			return new SqlSelectResult(sql, table, startRow, fetchSize, rows);
		}
		finally
		{
			QueryResultSet.close(qrs);
		}
	}

	/**
	 * 获取SQL查询结果。
	 * 
	 * @param cn
	 * @param st
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public SqlSelectResult select(Connection cn, String sql, ResultSet rs, int startRow, int fetchSize)
			throws SQLException
	{
		Table table = buildTable(cn, rs);

		List<Row> rows = mapToRows(cn, table, rs, startRow, fetchSize, null);
		return new SqlSelectResult(sql, table, startRow, fetchSize, rows);
	}

	/**
	 * 获取SQL查询结果。
	 * 
	 * @param cn
	 * @param st
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @param rowMapper
	 *            允许为{@code null}
	 * @return
	 * @throws SQLException
	 */
	public SqlSelectResult select(Connection cn, String sql, ResultSet rs, int startRow, int fetchSize,
			RowMapper rowMapper) throws SQLException
	{
		Table table = buildTable(cn, rs);

		List<Row> rows = mapToRows(cn, table, rs, startRow, fetchSize, rowMapper);
		return new SqlSelectResult(sql, table, startRow, fetchSize, rows);
	}

	public Table buildTable(Connection cn, ResultSet rs) throws SQLException
	{
		Column[] columns = this.dbMetaResolver.getColumns(cn, rs.getMetaData());
		return new Table(SELECT_TABLE_NAME, TableType.VIEW, columns);
	}
}
