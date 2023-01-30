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
	 * <p>
	 * 返回表的{@linkplain #isUserEntityDataTable(SimpleTable)}应为{@code true}。
	 * </p>
	 * 
	 * @param cn
	 * @return 可能返回{@code null}
	 * @throws DBMetaResolverException
	 */
	SimpleTable getRandomSimpleTable(Connection cn) throws DBMetaResolverException;

	/**
	 * 是否是用户数据表。
	 * <p>
	 * 用户数据表是用户可创建，且包含数据的表。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean isUserDataTable(Connection cn, SimpleTable table) throws DBMetaResolverException;

	/**
	 * 是否是用户数据实体表。
	 * <p>
	 * 用户数据实体表是用户可创建，且真实存储数据的表（非视图、别名、同义词）。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean isUserDataEntityTable(Connection cn, SimpleTable table) throws DBMetaResolverException;

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

	/**
	 * 获取导入键表名洌表。
	 * 
	 * @param cn
	 * @param tableNames
	 * @return
	 */
	List<String[]> getImportTables(Connection cn, String... tableNames);
}
