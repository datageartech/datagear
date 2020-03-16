/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 导入键规则。
 * <p>
 * 类结构参考{@linkplain DatabaseMetaData#getImportedKeys(String, String, String)}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public enum ImportedKeyRule
{
	NO_ACTION,

	CASCADE,

	SET_NULL,

	SET_DEFAUL,

	RESTRICT
}
