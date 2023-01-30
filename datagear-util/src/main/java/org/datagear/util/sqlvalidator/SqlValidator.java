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

package org.datagear.util.sqlvalidator;

/**
 * SQL校验器。
 * <p>
 * 校验SQL语句对于指定{@linkplain DatabaseProfile}是否合法，比如是否包含不允许出现的关键字。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface SqlValidator
{
	/**
	 * 校验。
	 * 
	 * @param sql
	 * @param profile
	 * @return
	 */
	SqlValidation validate(String sql, DatabaseProfile profile);
}
