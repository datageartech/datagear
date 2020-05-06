/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.util.Map;

/**
 * {@linkplain SqlDataSet}的SQL语句解析器。
 * <p>
 * 比如，当{@linkplain SqlDataSet#getSql()}是某种模板语言时。
 * </p>
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
	String resolve(SqlDataSet sqlDataSet, Map<String, ?> dataSetParamValues) throws SqlDataSetSqlResolverException;
}
