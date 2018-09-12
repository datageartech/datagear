/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import org.datagear.model.Model;

/**
 * {@linkplain Model}名称解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelNameResolver
{
	/**
	 * 解析{@linkplain Model}名称。
	 * 
	 * @param tableName
	 *            表名称
	 * @return
	 */
	String resolve(String tableName);
}
