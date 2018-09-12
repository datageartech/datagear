/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import java.sql.Connection;

import org.datagear.model.Model;

/**
 * 数据库{@linkplain Model}工厂。
 * 
 * @author datagear@163.com
 *
 */
public interface DbModelFactory
{
	/**
	 * 获取指定数据库表的{@linkplain Model}。
	 * 
	 * @param cn
	 * @param schema
	 * @param tableName
	 * @return
	 * @throws DbModelFactoryException
	 */
	Model getModel(Connection cn, String schema, String tableName) throws DbModelFactoryException;
}
