/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.persistence;

import java.sql.Connection;
import java.util.List;

import org.datagear.meta.Table;

/**
 * 持久化管理器。
 * 
 * @author datagear@163.com
 */
public interface PersistenceManager
{
	/**
	 * 获取{@linkplain DialectSource}。
	 * 
	 * @return
	 */
	DialectSource getDialectSource();

	/**
	 * 插入行对象。
	 * 
	 * @param cn
	 * @param table
	 * @param row
	 * @return 保存的行对象，可能包含自动生成列值
	 * @throws PersistenceException
	 */
	Row insert(Connection cn, Table table, Row row) throws PersistenceException;

	/**
	 * 插入行对象。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param row
	 * @param mapper
	 *            允许为{@code null}
	 * @return 保存的行对象，可能包含自动生成列值
	 * @throws PersistenceException
	 */
	Row insert(Connection cn, Dialect dialect, Table table, Row row, SqlParamValueMapper mapper)
			throws PersistenceException;

	/**
	 * 更新行对象。
	 * 
	 * @param cn
	 * @param table
	 * @param origin
	 *            原行
	 * @param update
	 *            更新行
	 * @return
	 * @throws PersistenceException
	 */
	int update(Connection cn, Table table, Row origin, Row update) throws PersistenceException;

	/**
	 * 更新行对象。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param origin
	 *            原行
	 * @param update
	 *            更新行
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	int update(Connection cn, Dialect dialect, Table table, Row origin, Row update, SqlParamValueMapper mapper)
			throws PersistenceException;

	/**
	 * 删除行对象。
	 * 
	 * @param cn
	 * @param table
	 * @param rows
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Table table, Row... rows) throws PersistenceException;

	/**
	 * 删除行对象。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param row
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Row row, SqlParamValueMapper mapper)
			throws PersistenceException;

	/**
	 * 删除行对象。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param rows
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Row[] rows, SqlParamValueMapper mapper)
			throws PersistenceException;

	/**
	 * 删除查询结果。
	 * 
	 * @param cn
	 * @param table
	 * @param query
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Table table, Query query) throws PersistenceException;

	/**
	 * 删除查询结果。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param query
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Query query) throws PersistenceException;

	/**
	 * 获取行对象。
	 * 
	 * @param cn
	 * @param table
	 * @param param
	 * @return
	 * @throws NonUniqueResultException
	 * @throws PersistenceException
	 */
	Row get(Connection cn, Table table, Row param) throws NonUniqueResultException, PersistenceException;

	/**
	 * 获取行对象。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param param
	 * @param sqlParamValueMapper
	 *            允许为{@code null}
	 * @param rowMapper
	 *            允许为{@code null}
	 * @return
	 * @throws NonUniqueResultException
	 * @throws PersistenceException
	 */
	Row get(Connection cn, Dialect dialect, Table table, Row param, SqlParamValueMapper sqlParamValueMapper,
			RowMapper rowMapper) throws NonUniqueResultException, PersistenceException;

	/**
	 * 查询。
	 * 
	 * @param cn
	 * @param table
	 * @param query
	 *            为{@code null}表示查询全部
	 * @return
	 * @throws PersistenceException
	 */
	List<Row> query(Connection cn, Table table, Query query) throws PersistenceException;

	/**
	 * 查询。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param query
	 *            为{@code null}表示查询全部
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	List<Row> query(Connection cn, Dialect dialect, Table table, Query query, RowMapper mapper)
			throws PersistenceException;

	/**
	 * 分页查询。
	 * 
	 * @param cn
	 * @param table
	 * @param pagingQuery
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Row> pagingQuery(Connection cn, Table table, PagingQuery pagingQuery) throws PersistenceException;

	/**
	 * 分页查询。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param pagingQuery
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Row> pagingQuery(Connection cn, Dialect dialect, Table table, PagingQuery pagingQuery, RowMapper mapper)
			throws PersistenceException;

	/**
	 * 获取查询SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param query
	 * @return
	 */
	String getQuerySql(Connection cn, Table table, Query query);

	/**
	 * 获取查询SQL语句。
	 * 
	 * @param cn
	 * @param dialect
	 *            允许为{@code null}
	 * @param table
	 * @param query
	 * @return
	 */
	String getQuerySql(Connection cn, Dialect dialect, Table table, Query query);
}
