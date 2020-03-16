/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.DataType;
import org.datagear.meta.Database;
import org.datagear.meta.PrimaryKey;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;

/**
 * 数据库元信息解析类。
 * 
 * @author datagear@163.com
 *
 */
public interface DBMetaResolver
{
	/**
	 * 获取{@linkplain Database}。
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	Database getDatabase(Connection cn) throws DBMetaResolverException;

	/**
	 * 获取所有{@linkplain SimpleTable}。
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<SimpleTable> getSimpleTables(Connection cn) throws DBMetaResolverException;

	/**
	 * 随机获取一个{@linkplain SimpleTable}。
	 * 
	 * @param cn
	 * @return 可能返回{@code null}
	 * @throws DBMetaResolverException
	 */
	SimpleTable getRandomSimpleTable(Connection cn) throws DBMetaResolverException;

	/**
	 * 获取指定名称的{@linkplain Table}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return
	 * @throws DBMetaResolverException
	 */
	Table getTable(Connection cn, String tableName) throws DBMetaResolverException;

	/**
	 * 获取指定表的所有{@linkplain Column}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return
	 * @throws DBMetaResolverException
	 */
	Column[] getColumns(Connection cn, String tableName) throws DBMetaResolverException;

	/**
	 * 获取指定表的一个随机{@linkplain Column}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return 返回{@code null}表示没找到任何字段
	 * @throws DBMetaResolverException
	 */
	Column getRandomColumn(Connection cn, String tableName) throws DBMetaResolverException;

	/**
	 * 获取指定{@linkplain ResultSetMetaData}的{@linkplain Column}。
	 * 
	 * @param cn
	 * @param resultSetMetaData
	 * @return
	 * @throws DBMetaResolverException
	 */
	Column[] getColumns(Connection cn, ResultSetMetaData resultSetMetaData) throws DBMetaResolverException;

	/**
	 * 获取{@linkplain PrimaryKey}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return 返回{@code null}表示无主键
	 * @throws DBMetaResolverException
	 */
	PrimaryKey getPrimaryKey(Connection cn, String tableName) throws DBMetaResolverException;

	/**
	 * 获取所有{@linkplain DataType}。
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<DataType> getDataTypes(Connection cn) throws DBMetaResolverException;
}
