/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
