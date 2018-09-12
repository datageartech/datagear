/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence;

import java.sql.Connection;

import org.datagear.connection.ConnectionSensor;

/**
 * 数据库方言构建器。
 * 
 * @author datagear@163.com
 *
 */
public interface DialectBuilder extends ConnectionSensor
{
	/**
	 * 构建{@linkplain Dialect}。
	 * 
	 * @param cn
	 * @return
	 * @throws DialectException
	 */
	Dialect build(Connection cn) throws DialectException;
}
