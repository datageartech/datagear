/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.persistence.support;

import java.util.List;

import org.datagear.meta.Table;
import org.datagear.persistence.Row;

/**
 * SQL查询结果。
 * 
 * @author datagear@163.com
 *
 */
public class SqlSelectResult
{
	private String sql;

	private Table table;

	private int startRow;

	private int fetchSize;

	private List<Row> rows;

	public SqlSelectResult()
	{
		super();
	}

	public SqlSelectResult(String sql, Table table, int startRow, int fetchSize, List<Row> rows)
	{
		super();
		this.sql = sql;
		this.table = table;
		this.startRow = startRow;
		this.fetchSize = fetchSize;
		this.rows = rows;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	public Table getTable()
	{
		return table;
	}

	public void setTable(Table table)
	{
		this.table = table;
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

	public List<Row> getRows()
	{
		return rows;
	}

	public void setRows(List<Row> rows)
	{
		this.rows = rows;
	}

	public boolean hasMoreRow()
	{
		return this.rows != null && this.rows.size() >= this.fetchSize;
	}

	public int getNextStartRow()
	{
		return this.startRow + this.fetchSize;
	}
}