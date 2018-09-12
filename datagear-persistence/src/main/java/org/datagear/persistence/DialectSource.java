/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence;

import java.sql.Connection;

/**
 * {@linkplain Dialect}源。
 * 
 * @author datagear@163.com
 *
 */
public interface DialectSource
{
	/**
	 * 获取给定数据库连接对应的{@linkplain Dialect}。
	 * 
	 * @param cn
	 * @return
	 * @throws DialectException
	 */
	Dialect getDialect(Connection cn) throws DialectException;
}
