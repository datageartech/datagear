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
public interface DBMetaResolver extends TableTypeResolver
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
	 * 获取当前连接用户的所有表。
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<SimpleTable> getTables(Connection cn) throws DBMetaResolverException;

	/**
	 * 获取当前连接用户的所有数据表。
	 * <p>
	 * 返回表的{@linkplain #isDataTable(Connection, SimpleTable)}为{@code true}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<SimpleTable> getDataTables(Connection cn) throws DBMetaResolverException;

	/**
	 * 获取当前连接用户的所有实体表。
	 * <p>
	 * 返回表的{@linkplain #isEntityTable(Connection, SimpleTable)}为{@code true}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<SimpleTable> getEntityTables(Connection cn) throws DBMetaResolverException;

	/**
	 * 随机获取当前连接用户的一个数据表。
	 * <p>
	 * 返回表的{@linkplain #isDataTable(Connection, SimpleTable)}应为{@code true}。
	 * </p>
	 * 
	 * @param cn
	 * @return 可能返回{@code null}
	 * @throws DBMetaResolverException
	 */
	SimpleTable getRandomDataTable(Connection cn) throws DBMetaResolverException;

	/**
	 * 获取准确表名。
	 * <p>
	 * 某些数据库的表名可能会区分大小写，导致此类的其他方法异常（比如{@linkplain #getTable(Connection, String)}会在给定的表名参数大小写不匹配时报找不到表错误），
	 * 此时，可以使用这个方法获取准确表名后再尝试。
	 * </p>
	 * 
	 * @param cn
	 * @param tableName
	 * @return 数据库中准确使用的表名，可能是与{@code tableName}相同的，或者大小写不同的，{@code null}表示不存在
	 * @throws DBMetaResolverException
	 */
	String getExactTableName(Connection cn, String tableName) throws DBMetaResolverException;

	/**
	 * 获取准确表名。
	 * 
	 * @param cn
	 * @param tableNames
	 * @return 元素为{@code null}表示不存在
	 * @throws DBMetaResolverException
	 * @see {@linkplain #getExactTableName(Connection, String)}
	 */
	String[] getExactTableNames(Connection cn, String[] tableNames) throws DBMetaResolverException;

	/**
	 * 获取指定名称的{@linkplain Table}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return
	 * @throws TableNotFoundException
	 * @throws DBMetaResolverException
	 */
	Table getTable(Connection cn, String tableName) throws TableNotFoundException, DBMetaResolverException;

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
	 * 获取导入键表名列表。
	 * 
	 * @param cn
	 * @param tableNames
	 *            元素允许为{@code null}
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<String[]> getImportTables(Connection cn, String... tableNames) throws DBMetaResolverException;
}
