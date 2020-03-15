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
 * @createDate 2014年11月28日
 */
public interface PersistenceManager
{
	/**
	 * 插入行。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param row
	 * @param converter
	 * @return
	 * @throws PersistenceException
	 */
	int insert(Connection cn, Dialect dialect, Table table, Row row, PstValueConverter converter)
			throws PersistenceException;

	/**
	 * 更新行。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param origin    原行
	 * @param update    更新行
	 * @param converter
	 * @return
	 * @throws PersistenceException
	 */
	int update(Connection cn, Dialect dialect, Table table, Row origin, Row update, PstValueConverter converter)
			throws PersistenceException;

	/**
	 * 删除行。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param row
	 * @param converter
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Row row, PstValueConverter converter)
			throws PersistenceException;

	/**
	 * 删除多行。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param rows
	 * @param converter
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Row[] rows, PstValueConverter converter)
			throws PersistenceException;

	/**
	 * 删除查询结果。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param query
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Dialect dialect, Table table, Query query) throws PersistenceException;

	/**
	 * 获取行。
	 * 
	 * @param cn
	 * @param table
	 * @param param
	 * @param converter
	 * @param mapper
	 * @return
	 * @throws NotUniqueRowException
	 * @throws PersistenceException
	 */
	Row get(Connection cn, Dialect dialect, Table table, Row param, PstValueConverter converter, RowMapper mapper)
			throws NotUniqueRowException, PersistenceException;

	/**
	 * 查询。
	 * 
	 * @param cn
	 * @param table
	 * @param query  为{@code null}表示查询全部
	 * @param mapper
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
	 * @param mapper
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Row> pagingQuery(Connection cn, Dialect dialect, Table table, PagingQuery pagingQuery, RowMapper mapper)
			throws PersistenceException;
}
