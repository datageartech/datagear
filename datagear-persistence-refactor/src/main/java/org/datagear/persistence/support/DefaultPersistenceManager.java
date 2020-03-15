/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.PstValueConverter;
import org.datagear.persistence.Query;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.persistence.Sql;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;

/**
 * 默认{@linkplain PersistenceManager}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPersistenceManager extends PersistenceSupport implements PersistenceManager
{
	public DefaultPersistenceManager()
	{
		super();
	}

	@Override
	public int insert(Connection cn, Dialect dialect, Table table, Row row, PstValueConverter converter)
			throws PersistenceException
	{
		Sql sql = Sql.valueOf().sql("INSERT INTO ").sql(quote(dialect, table.getName())).sql(" (")
				.delimit(",");
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

			value = converter.convert(cn, dialect, table, column, value);

			sql.sqld(quote(dialect, name));
			valueSql.sqld("?").param(value, column.getType());
		}

		sql.sql(")");
		valueSql.sql(")");
		sql.sql(valueSql);

		return executeUpdate(cn, sql);
	}

	@Override
	public int update(Connection cn, Dialect dialect, Table table, Row origin, Row update, PstValueConverter converter)
			throws PersistenceException
	{
		Sql sql = Sql.valueOf().sql("UPDATE ").sql(quote(dialect, table.getName())).sql(" SET ").delimit(",");

		Column[] columns = table.getColumns();

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			if (!update.containsKey(name))
				continue;

			Object value = update.get(name);

			value = converter.convert(cn, dialect, table, column, value);

			sql.sqld(quote(dialect, name) + "=?").param(value, column.getType());
		}

		sql.sql(" WHERE ").sql(buildUniqueRowCondition(cn, dialect, table, origin, converter));

		return executeUpdate(cn, sql);
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Row row, PstValueConverter converter)
			throws PersistenceException
	{
		return delete(cn, dialect, table, new Row[] { row }, converter);
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Row[] rows, PstValueConverter converter)
			throws PersistenceException
	{
		Sql sql = Sql.valueOf().sql("DELETE FROM ").sql(quote(dialect, table.getName())).sql(" WHERE ")
				.delimit(" AND ");

		Column[] columns = getUniqueRowColumns(table);

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			sql.sqld(quote(dialect, name) + "=?");
		}

		PreparedStatement pst = null;

		try
		{
			pst = createPreparedStatement(cn, sql);

			int count = 0;

			for (int i = 0; i < rows.length; i++)
			{
				Row row = rows[i];
				for (int j = 0; j < columns.length; j++)
				{
					Column column = columns[i];
					String name = column.getName();
					Object value = row.get(name);

					value = converter.convert(cn, dialect, table, column, value);

					sql.param(value, column.getType());
				}

				setParamValues(cn, pst, sql);
				count += pst.executeUpdate();

				sql.clearParam();
				pst.clearParameters();
			}

			return count;
		}
		catch(SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			JdbcUtil.closeStatement(pst);
		}
	}

	@Override
	public int delete(Connection cn, Dialect dialect, Table table, Query query) throws PersistenceException
	{
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Row get(Connection cn, Dialect dialect, Table table, Row param, PstValueConverter converter,
			RowMapper mapper) throws PersistenceException
	{
		Sql sql = Sql.valueOf().sql("SELECT * FROM ").sql(quote(dialect, table.getName())).sql(" WHERE ")
				.delimit(" AND ");

		Column[] columns = getUniqueRowColumns(table);

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();
			Object value = param.get(name);

			value = converter.convert(cn, dialect, table, column, value);

			sql.sqld(quote(dialect, name) + "=?").param(value, column.getType());
		}

		List<Row> rows = executeListQuery(cn, table, sql, mapper);

		if (rows.size() > 1)
			throw new NonUniqueResultException();

		return rows.get(0);
	}

	@Override
	public List<Row> query(Connection cn, Dialect dialect, Table table, Query query, RowMapper mapper)
			throws PersistenceException
	{
		Sql sql = buildQuery(cn, dialect, table, query);
		return executeListQuery(cn, table, sql, mapper);
	}

	@Override
	public PagingData<Row> pagingQuery(Connection cn, Dialect dialect, Table table, PagingQuery pagingQuery,
			RowMapper mapper)
			throws PersistenceException
	{
		Sql queryView = buildQuery(cn, dialect, table, pagingQuery);

		long total = executeCountQuery(cn, queryView);

		PagingData<Row> pagingData = new PagingData<Row>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		Sql query = null;

		int startRow = pagingData.getStartRow();
		int count = pagingData.getPageSize();

		// 数据库分页
		if (dialect.supportsPagingSql())
		{
			query = dialect.toPagingQuerySql(queryView, pagingQuery.getOrders(), startRow, count);

			if (query != null)
			{
				startRow = 1;
				count = -1;
			}
		}

		// 内存分页
		if (query == null)
			query = dialect.toOrderSql(queryView, pagingQuery.getOrders());

		List<Row> rows = executeListQuery(cn, table, query, mapper, startRow, count);

		pagingData.setItems(rows);

		return pagingData;
	}

	protected long queryCount(Connection cn, Sql query)
	{
		Sql countQuery = Sql.valueOf().sql("SELECT COUNT(*) FROM (").sql(query).sql(") T");

		long re = executeCountQuery(cn, countQuery);

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
	 * 尝试构建能够确定唯一行的查询条件。
	 * <p>
	 * 注意：如果表没有主键和唯一键，返回的查询结果不一定是能够确定唯一行的。
	 * </p>
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param row
	 * @param converter
	 * @return
	 * @throws PersistenceException
	 */
	protected Sql buildUniqueRowCondition(Connection cn, Dialect dialect, Table table, Row row,
			PstValueConverter converter) throws PersistenceException
	{
		Column[] columns = getUniqueRowColumns(table);

		Sql sql = Sql.valueOf().delimit(" AND ");

		for (int i = 0; i < columns.length; i++)
		{
			Column column = columns[i];
			String name = column.getName();

			Object value = row.get(name);

			value = converter.convert(cn, dialect, table, column, value);

			sql.sqld(quote(dialect, name) + "=?").param(value, column.getType());
		}

		return sql;
	}

	/**
	 * 尝试获取能确定唯一行的列数组。
	 * 
	 * @param table
	 * @return
	 * @throws NoUniqueRowColumnException
	 */
	protected Column[] getUniqueRowColumns(Table table) throws NoUniqueRowColumnException
	{
		Column[] columns = null;

		if (table.hasPrimaryKey())
			columns = table.getColumns(table.getPrimaryKey().getColumnNames());
		else if (table.hasUniqueKey())
			columns = table.getColumns(table.getUniqueKeys()[0].getColumnNames());
		else
			columns = getColumnsMaybeUniqueRow(table);

		if (columns == null || columns.length == 0)
			throw new NoUniqueRowColumnException("can not build unique row condition");

		return columns;
	}

	/**
	 * 获取可能用于作为行唯一标识的{@linkplain Column}数组。
	 * 
	 * @param table
	 * @return
	 */
	protected Column[] getColumnsMaybeUniqueRow(Table table)
	{
		List<Column> re = new ArrayList<Column>();

		Column[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++)
		{
			int jdbcTypeValue = columns[i].getType();
			
			if (Types.BIGINT == jdbcTypeValue || Types.BIT == jdbcTypeValue
					|| Types.BOOLEAN == jdbcTypeValue || Types.CHAR == jdbcTypeValue
					|| Types.DATE == jdbcTypeValue || Types.DECIMAL == jdbcTypeValue
					|| Types.DOUBLE == jdbcTypeValue || Types.FLOAT == jdbcTypeValue
					|| Types.INTEGER == jdbcTypeValue || Types.NULL == jdbcTypeValue
					|| Types.NUMERIC == jdbcTypeValue || Types.REAL == jdbcTypeValue
					|| Types.SMALLINT == jdbcTypeValue || Types.TIME == jdbcTypeValue
					|| Types.TIMESTAMP == jdbcTypeValue || Types.TINYINT == jdbcTypeValue
					|| Types.VARCHAR == jdbcTypeValue)
				re.add(columns[i]);
		}

		return re.toArray(new Column[re.size()]);
	}
}
