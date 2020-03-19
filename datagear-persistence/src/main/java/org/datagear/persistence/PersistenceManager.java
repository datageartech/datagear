/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
	 * @return
	 * @throws PersistenceException
	 */
	int insert(Connection cn, Table table, Row row) throws PersistenceException;

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
	 * @return
	 * @throws PersistenceException
	 */
	int insert(Connection cn, Dialect dialect, Table table, Row row, SqlParamValueMapper mapper)
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
	 * @param dialect             允许为{@code null}
	 * @param table
	 * @param param
	 * @param sqlParamValueMapper 允许为{@code null}
	 * @param rowMapper           允许为{@code null}
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
}
