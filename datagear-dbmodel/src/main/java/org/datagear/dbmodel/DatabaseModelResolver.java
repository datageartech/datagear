/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.model.Model;
import org.datagear.model.ModelManager;

/**
 * 数据库{@link Model}解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface DatabaseModelResolver
{
	/**
	 * 解析指定表的{@link Model}。
	 * 
	 * @param cn
	 *            数据库连接
	 * @param globalModelManager
	 *            全局{@linkplain ModelManager}，仅用于查找已被解析的关联表{@link Model}
	 * @param localModelManager
	 *            局部{@linkplain ModelManager}，用于存储本次解析新产生的关联表{@link Model}
	 *            ，此方法的返回结果{@link Model}也将存储于此
	 * @param table
	 *            待解析表名称
	 * @return
	 * @throws DatabaseModelResolverException
	 */
	Model resolve(Connection cn, ModelManager globalModelManager, ModelManager localModelManager, String table)
			throws DatabaseModelResolverException;

	/**
	 * 解析指定结果集的{@linkplain Model}。
	 * 
	 * @param cn
	 * @param resultSet
	 * @param modelName
	 * @return
	 * @throws DatabaseModelResolverException
	 */
	Model resolve(Connection cn, ResultSet resultSet, String modelName) throws DatabaseModelResolverException;
}
