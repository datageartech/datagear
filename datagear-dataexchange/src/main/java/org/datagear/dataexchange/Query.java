/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;

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
	ResultSet execute(Connection cn) throws Throwable;
}
