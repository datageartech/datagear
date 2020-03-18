/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.Query;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.persistence.SqlParamValueMapper;
import org.datagear.util.JdbcUtil;
import org.datagear.util.Sql;
import org.datagear.util.SqlParamValue;
import org.datagear.util.StringUtil;

/**
 * 默认{@linkplain PersistenceManager}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPersistenceManager extends PersistenceSupport implements PersistenceManager
{
	private DialectSource dialectSource;

	public DefaultPersistenceManager()
	{
		super();
	}

	public DefaultPersistenceManager(DialectSource dialectSource)
	{
		super();
		this.dialectSource = dialectSource;
	}

	@Override
	public DialectSource getDialectSource()
	{
		return dialectSource;
	}

	public void setDialectSource(DialectSource dialectSource)
	{
		this.dialectSource = dialectSource;
	}

	@Override
	public int insert(Connection cn, Table table, Row row) throws PersistenceException
	{
		return insert(cn, null, table, row, null);
	}

	@Override
	public int insert(Connection cn, Dialect dialect, Table table, Row row, SqlParamValueMapper mapper)
			throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql sql = Sql.valueOf().sql("INSERT INTO ").sql(quote(dialect, table.getName())).sql(" (").delimit(",");
		Sql valueSql = Sql.valueOf().sql(" VALUES (").delimit(",");

		Column[] columns = table.getColumns();

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			if (!row.containsKey(name))
				continue;

			Object value = row.get(name);

			if (value == null && column.isAutoincrement())
				continue;

			if (value == null && column.hasDefaultValue())
				value = column.getDefaultValue();

			SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value, mapper);

			sql.sqld(quote(dialect, name));
			valueSql.sqld("?").param(sqlParamValue);
		}

		sql.sql(")");
		valueSql.sql(")");
		sql.sql(valueSql);

		return executeUpdateWrap(cn, sql);
	}

	@Override
	public int update(Connection cn, Table table, Row origin, Row update) throws PersistenceException
	{
		return update(cn, null, table, origin, update, null);
	}

	@Override
	public int update(Connection cn, Dialect dialect, Table table, Row origin, Row update, SqlParamValueMapper mapper)
			throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql sql = Sql.valueOf().sql("UPDATE ").sql(quote(dialect, table.getName())).sql(" SET ").delimit(",");

		Column[] columns = table.getColumns();

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			if (!update.containsKey(name))
				continue;

			Object value = update.get(name);

			SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value, mapper);

			sql.sqld(quote(dialect, name) + "=?").param(sqlParamValue);
		}

		sql.sql(" WHERE ").sql(buildUniqueRecordCondition(cn, dialect, table, origin, mapper));

		return executeUpdateWrap(cn, sql);
	}

	@Override
	public int delete(Connection cn, Table table, Row... rows) throws PersistenceException
	{
		return delete(cn, null, table, rows, null);
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Row row, SqlParamValueMapper mapper)
			throws PersistenceException
	{
		return delete(cn, dialect, table, new Row[] { row }, mapper);
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Row[] rows, SqlParamValueMapper mapper)
			throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql sql = Sql.valueOf().sql("DELETE FROM ").sql(quote(dialect, table.getName())).sql(" WHERE ")
				.delimit(" AND ");

		Column[] columns = getUniqueRecordColumns(table);

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			sql.sqld(quote(dialect, name) + "=?");
		}

		PreparedStatement pst = null;

		try
		{
			pst = createUpdatePreparedStatement(cn, sql.getSqlValue());

			int count = 0;

			for (int i = 0; i < rows.length; i++)
			{
				Row row = rows[i];
				for (int j = 0; j < columns.length; j++)
				{
					Column column = columns[i];
					String name = column.getName();
					Object value = row.get(name);

					SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value, mapper);

					sql.param(sqlParamValue);
				}

				setParamValues(cn, pst, sql);
				count += pst.executeUpdate();

				sql.clearParam();
				pst.clearParameters();
			}

			return count;
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

	@Override
	public int delete(Connection cn, Table table, Query query) throws PersistenceException
	{
		return delete(cn, null, table, query);
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Query query) throws PersistenceException
	{
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Row get(Connection cn, Table table, Row param) throws PersistenceException
	{
		return get(cn, null, table, param, null, null);
	}

	@Override
	public Row get(Connection cn, Dialect dialect, Table table, Row param, SqlParamValueMapper sqlParamValueMapper,
			RowMapper rowMapper) throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql sql = Sql.valueOf().sql("SELECT * FROM ").sql(quote(dialect, table.getName())).sql(" WHERE ")
				.delimit(" AND ");

		Column[] columns = getUniqueRecordColumns(table);

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();
			Object value = param.get(name);

			SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value, sqlParamValueMapper);

			sql.sqld(quote(dialect, name) + "=?").param(sqlParamValue);
		}

		List<Row> rows = executeListQuery(cn, table, sql, ResultSet.TYPE_FORWARD_ONLY, rowMapper);

		if (rows.size() > 1)
			throw new NonUniqueResultException();

		return rows.get(0);
	}

	@Override
	public List<Row> query(Connection cn, Table table, Query query) throws PersistenceException
	{
		return query(cn, null, table, query, null);
	}

	@Override
	public List<Row> query(Connection cn, Dialect dialect, Table table, Query query, RowMapper mapper)
			throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql sql = buildQuery(cn, dialect, table, query);
		return executeListQuery(cn, table, sql, ResultSet.TYPE_FORWARD_ONLY, mapper);
	}

	@Override
	public PagingData<Row> pagingQuery(Connection cn, Table table, PagingQuery pagingQuery) throws PersistenceException
	{
		return pagingQuery(cn, null, table, pagingQuery, null);
	}

	@Override
	public PagingData<Row> pagingQuery(Connection cn, Dialect dialect, Table table, PagingQuery pagingQuery,
			RowMapper mapper) throws PersistenceException
	{
		dialect = getDialect(cn, dialect);

		Sql queryView = buildQuery(cn, dialect, table, pagingQuery);

		long total = queryCount(cn, queryView);

		PagingData<Row> pagingData = new PagingData<>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		Sql query = null;
		List<Row> rows = null;
		int startRow = pagingData.getStartRow();
		int count = pagingData.getPageSize();

		if (dialect.supportsPagingSql())
		{
			query = dialect.toPagingQuerySql(queryView, pagingQuery.getOrders(), startRow, count);

			// 数据库分页
			if (query != null)
			{
				startRow = 1;
				count = -1;
			}
		}

		// 内存分页
		if (query == null)
			query = dialect.toOrderSql(queryView, pagingQuery.getOrders());

		rows = executeListQuery(cn, table, query, ResultSet.TYPE_SCROLL_INSENSITIVE, startRow, count, mapper);

		pagingData.setItems(rows);

		return pagingData;
	}

	protected long queryCount(Connection cn, Sql query)
	{
		Sql countQuery = Sql.valueOf().sql("SELECT COUNT(*) FROM (").sql(query).sql(") T");

		long re = executeCountQueryWrap(cn, countQuery);

		return re;
	}

	protected Sql buildQuery(Connection cn, Dialect dialect, Table table, Query query)
	{
		Sql sql = Sql.valueOf().sql("SELECT * FROM ").sql(quote(dialect, table.getName()));
		Sql condition = buildQueryCondition(cn, dialect, table, query);

		if (!Sql.isEmpty(condition))
		{
			sql.sql(" WHERE ");
			sql.sql(condition);
		}

		return sql;
	}

	/**
	 * 构建查询条件。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param query
	 * @return 返回{@code null}表示无条件
	 */
	protected Sql buildQueryCondition(Connection cn, Dialect dialect, Table table, Query query)
	{
		if (query == null)
			return null;

		if (!query.hasKeyword() && !query.hasCondition())
			return null;

		String conditionStr = (query.hasCondition() ? query.getCondition().trim() : null);
		boolean hasCondition = !StringUtil.isEmpty(conditionStr);

		Sql keywordCondition = dialect.toKeywordQueryCondition(table, query);

		if (!hasCondition)
		{
			return keywordCondition;
		}
		else
		{
			Sql sql = Sql.valueOf();

			if (Sql.isEmpty(keywordCondition))
				sql.sql(conditionStr);
			else
				sql.sql("(").sql(conditionStr).sql(")").sql(" AND (").sql(keywordCondition).sql(")");

			return sql;
		}
	}

	/**
	 * 尝试构建能够确定唯一行记录的查询条件。
	 * <p>
	 * 注意：如果表没有主键和唯一键，返回的查询结果不一定是能够确定唯一行的。
	 * </p>
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param row
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	protected Sql buildUniqueRecordCondition(Connection cn, Dialect dialect, Table table, Row row,
			SqlParamValueMapper mapper) throws PersistenceException
	{
		Column[] columns = getUniqueRecordColumns(table);

		Sql sql = Sql.valueOf().delimit(" AND ");

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			Object value = row.get(name);

			SqlParamValue sqlParamValue = mapToSqlParamValue(cn, table, column, value, mapper);

			sql.sqld(quote(dialect, name) + "=?").param(sqlParamValue);
		}

		return sql;
	}

	/**
	 * 尝试获取能确定唯一行记录的列数组。
	 * 
	 * @param table
	 * @return
	 * @throws NonUniqueRecordColumnException
	 */
	protected Column[] getUniqueRecordColumns(Table table) throws NonUniqueRecordColumnException
	{
		Column[] columns = null;

		if (table.hasPrimaryKey())
			columns = table.getColumns(table.getPrimaryKey().getColumnNames());
		else if (table.hasUniqueKey())
			columns = table.getColumns(table.getUniqueKeys()[0].getColumnNames());
		else
			columns = getColumnsMaybeUniqueRecord(table);

		if (columns == null || columns.length == 0)
			throw new NonUniqueRecordColumnException("can not build unique row condition");

		return columns;
	}

	/**
	 * 获取可能用于作为行唯一行记标识的{@linkplain Column}数组。
	 * 
	 * @param table
	 * @return
	 */
	protected Column[] getColumnsMaybeUniqueRecord(Table table)
	{
		List<Column> re = new ArrayList<>();

		Column[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++)
		{
			int jdbcTypeValue = columns[i].getType();

			if (Types.BIGINT == jdbcTypeValue || Types.BIT == jdbcTypeValue || Types.BOOLEAN == jdbcTypeValue
					|| Types.CHAR == jdbcTypeValue || Types.DATE == jdbcTypeValue || Types.DECIMAL == jdbcTypeValue
					|| Types.DOUBLE == jdbcTypeValue || Types.FLOAT == jdbcTypeValue || Types.INTEGER == jdbcTypeValue
					|| Types.NULL == jdbcTypeValue || Types.NUMERIC == jdbcTypeValue || Types.REAL == jdbcTypeValue
					|| Types.SMALLINT == jdbcTypeValue || Types.TIME == jdbcTypeValue
					|| Types.TIMESTAMP == jdbcTypeValue || Types.TINYINT == jdbcTypeValue
					|| Types.VARCHAR == jdbcTypeValue)
				re.add(columns[i]);
		}

		return re.toArray(new Column[re.size()]);
	}

	/**
	 * 
	 * @param cn
	 * @param table
	 * @param column
	 * @param value
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	protected SqlParamValue mapToSqlParamValue(Connection cn, Table table, Column column, Object value,
			SqlParamValueMapper mapper) throws PersistenceException
	{
		if (mapper == null)
			return toSqlParamValue(column, value);

		return mapper.map(cn, table, column, value);
	}

	/**
	 * 
	 * @param cn
	 * @param init
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	protected Dialect getDialect(Connection cn, Dialect init) throws PersistenceException
	{
		if (init != null)
			return init;

		return this.dialectSource.getDialect(cn);
	}
}
