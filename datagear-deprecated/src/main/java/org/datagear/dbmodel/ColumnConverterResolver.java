/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.EntireTableInfo;
import org.datagear.persistence.features.ColumnConverter;

/**
 * {@linkplain ColumnConverter}解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface ColumnConverterResolver
{
	/**
	 * 解析指定{@linkplain ColumnInfo}对应的基本{@linkplain ColumnConverter}。
	 * <p>
	 * 返回{@code null}表示不需要。
	 * </p>
	 * 
	 * @param cn
	 * @param entireTableInfo
	 * @param columnInfo
	 * @return
	 */
	ColumnConverter resolve(Connection cn, EntireTableInfo entireTableInfo, ColumnInfo columnInfo);
}
