/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.EntireTableInfo;
import org.datagear.model.Model;

/**
 * 基本{@linkplain Model}解析器。
 * 
 * @author datagear@163.com
 *
 */
public interface PrimitiveModelResolver
{
	/**
	 * 解析指定{@linkplain ColumnInfo}对应的基本{@linkplain Model}。
	 * <p>
	 * 返回{@code null}表示无法解析。
	 * </p>
	 * 
	 * @param cn
	 * @param entireTableInfo
	 * @param columnInfo
	 * @return
	 */
	Model resolve(Connection cn, EntireTableInfo entireTableInfo, ColumnInfo columnInfo);
}
