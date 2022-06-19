/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import java.sql.Connection;

import org.datagear.util.QueryResultSet;

/**
 * 查询。
 * 
 * @author datagear@163.com
 *
 */
public interface Query
{
	/**
	 * 执行查询并返回结果。
	 * 
	 * @param cn
	 * @return
	 * @throws Throwable
	 */
	QueryResultSet execute(Connection cn) throws Throwable;
}
