/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import org.datagear.model.Model;

/**
 * 支持缓存的{@linkplain DbModelFactory}。
 * 
 * @author datagear@163.com
 *
 */
public interface CachedDbModelFactory extends DbModelFactory
{
	/**
	 * 获取缓存中的{@linkplain Model}。
	 * <p>
	 * 如果缓存中没有，将返回{@code null}。
	 * </p>
	 * 
	 * @param schema
	 * @param tableName
	 * @return
	 */
	Model getCachedModel(String schema, String tableName);

	/**
	 * 移除缓存中的指定{@code schema}的{@linkplain Model}。
	 * 
	 * @param schema
	 */
	void removeCachedModel(String schema);

	/**
	 * 移除缓存中的指定{@code schema}的{@linkplain Model}。
	 * 
	 * @param schema
	 * @param tableName
	 */
	void removeCachedModel(String schema, String tableName);
}
