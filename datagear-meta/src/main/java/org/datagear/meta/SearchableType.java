/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta;

import java.sql.DatabaseMetaData;

/**
 * 可搜索类型（WHERE条件类型）。
 * <p>
 * 参考{@linkplain DatabaseMetaData#getTypeInfo()}结果集{@code SEARCHABLE}列说明。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public enum SearchableType
{
	/** 不可用于WHERE */
	NO,

	/** 仅可用于WHERE中的LIKE */
	ONLY_LIKE,

	/** 仅不可用于WHERE中的LIKE */
	EXPCEPT_LIKE,

	/** 可用于WHERE中的任何情况 */
	ALL
}
