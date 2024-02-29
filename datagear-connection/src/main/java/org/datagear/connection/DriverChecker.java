/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.connection;

import java.sql.Driver;

/**
 * {@linkplain Driver}校验器。
 * 
 * @author datagear@163.com
 *
 */
public interface DriverChecker
{
	/**
	 * 校验{@linkplain Driver}是否支持使用给定{@linkplain ConnectionOption}建立连接。
	 * <p>
	 * 返回{@code false}或者抛出任何异常，表明不支持。
	 * </p>
	 * 
	 * @param driver
	 * @param connectionOption
	 * @param ignoreAcceptsURLCheck
	 *            是否忽略{@linkplain Driver#acceptsURL(String)}校验。
	 * @return
	 * @throws Throwable
	 */
	boolean check(Driver driver, ConnectionOption connectionOption, boolean ignoreAcceptsURLCheck) throws Throwable;
}
