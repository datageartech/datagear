/*
 * Copyright 2018-2024 datagear.tech
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
import java.util.List;

import org.datagear.meta.SimpleTable;

/**
 * 表类型解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface TableTypeResolver
{
	/**
	 * 获取所有表类型。
	 * 
	 * @param cn
	 * @return {@code null}表示没有取得任何表类型
	 * @throws DBMetaResolverException
	 */
	String[] getTableTypes(Connection cn) throws DBMetaResolverException;

	/**
	 * 是否是数据表。
	 * <p>
	 * 数据表是指可查询数据的表，比如：表、视图、别名、同义词。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean isDataTable(Connection cn, SimpleTable table) throws DBMetaResolverException;

	/**
	 * 是否是数据表。
	 * <p>
	 * 数据表是指可查询数据的表，比如：表、视图、别名、同义词。
	 * </p>
	 * 
	 * @param cn
	 * @param tables
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean[] isDataTables(Connection cn, SimpleTable[] tables) throws DBMetaResolverException;

	/**
	 * 是否是数据表。
	 * <p>
	 * 数据表是指可查询数据的表，比如：表、视图、别名、同义词。
	 * </p>
	 * 
	 * @param cn
	 * @param tables
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<Boolean> isDataTables(Connection cn, List<? extends SimpleTable> tables) throws DBMetaResolverException;

	/**
	 * 是否是实体表。
	 * <p>
	 * 实体表是指可查询、可写入数据的表（非视图、别名、同义词）。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean isEntityTable(Connection cn, SimpleTable table) throws DBMetaResolverException;

	/**
	 * 是否是实体表。
	 * <p>
	 * 实体表是指可查询、可写入数据的表（非视图、别名、同义词）。
	 * </p>
	 * 
	 * @param cn
	 * @param tables
	 * @return
	 * @throws DBMetaResolverException
	 */
	boolean[] isEntityTables(Connection cn, SimpleTable[] tables) throws DBMetaResolverException;

	/**
	 * 是否是实体表。
	 * <p>
	 * 实体表是指可查询、可写入数据的表（非视图、别名、同义词）。
	 * </p>
	 * 
	 * @param cn
	 * @param tables
	 * @return
	 * @throws DBMetaResolverException
	 */
	List<Boolean> isEntityTables(Connection cn, List<? extends SimpleTable> tables) throws DBMetaResolverException;
}
