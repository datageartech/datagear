/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 表类型。
 * <p>
 * 类结构参考
 * {@linkplain DatabaseMetaData#getTables(String, String, String, String[])}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public enum TableType
{
	/** 实体表 */
	TABLE,

	/** 视图 */
	VIEW,

	/** 别名 */
	ALIAS
}
