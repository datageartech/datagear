/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.util.Map;

/**
 * 模板语言SQL语句解析器。
 * <p>
 * 此类解析由某种模板语言（比如Freemarker）构建的SQL语句，并返回真实可执行的SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface TemplateSqlResolver
{
	/**
	 * 解析。
	 * 
	 * @param sql
	 * @param values
	 * @return
	 * @throws TemplateSqlResolverException
	 */
	String resolve(String sql, Map<String, ?> values) throws TemplateSqlResolverException;
}
