/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver;

import java.sql.Connection;
import java.util.List;

import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;

/**
 * 数据库元信息解析类。
 * 
 * @author datagear@163.com
 *
 */
public interface DatabaseMetaResolver
{
	/**
	 * 获取所有{@linkplain SimpleTable}。
	 * 
	 * @param cn
	 * @return
	 * @throws DatabaseMetaResolverException
	 */
	List<SimpleTable> getSimpleTables(Connection cn) throws DatabaseMetaResolverException;

	/**
	 * 获取指定名称的{@linkplain Table}。
	 * 
	 * @param cn
	 * @param tableName
	 * @return
	 * @throws DatabaseMetaResolverException
	 */
	Table getTable(Connection cn, String tableName) throws DatabaseMetaResolverException;
}
