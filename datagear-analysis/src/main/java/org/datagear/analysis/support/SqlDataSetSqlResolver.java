/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.util.Map;

import org.datagear.util.Sql;

/**
 * {@linkplain SqlDataSet}至{@linkplain Sql}解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface SqlDataSetSqlResolver
{
	/**
	 * 解析。
	 * 
	 * @param sqlDataSet
	 * @param dataSetParamValues
	 * @return
	 * @throws SqlDataSetSqlResolverException
	 */
	Sql resolve(SqlDataSet sqlDataSet, Map<String, ?> dataSetParamValues) throws SqlDataSetSqlResolverException;
}
