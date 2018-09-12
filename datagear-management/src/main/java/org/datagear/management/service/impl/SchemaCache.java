/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service.impl;

import java.util.Set;

import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;

/**
 * {@linkplain Schema}缓存。
 * <p>
 * {@linkplain SchemaService#getById(String)}再会在建立数据库连接时频繁访问，这里使用缓存可以提升性能。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface SchemaCache
{
	/**
	 * 从缓存中读取{@linkplain Schema}。
	 * <p>
	 * 如果缓存中没有，此方法返回{@code null}。
	 * </p>
	 * 
	 * @param schemaId
	 * @return
	 */
	Schema getSchema(String schemaId);

	/**
	 * 移除缓存中指定ID的{@linkplain Schema}。
	 * 
	 * @param schemaId
	 */
	void removeSchema(String schemaId);

	/**
	 * 将指定{@linkplain Schema}放入缓存中。
	 * 
	 * @param schema
	 */
	void putSchema(Schema schema);

	/**
	 * 获取缓存中的所有{@linkplain Schema#getId()}。
	 * 
	 * @return
	 */
	Set<String> getAllSchemaIds();
}
