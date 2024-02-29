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
import java.util.Properties;

/**
 * 连接参数处理器。
 * <p>
 * {@linkplain DefaultConnectionSource}使用此类在获取连接之前处理连接参数。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface PropertiesProcessor
{
	/**
	 * 处理连接参数。
	 * <p>
	 * 比如修改已设参数，或者添加新参数。
	 * </p>
	 * 
	 * @param driver
	 * @param url
	 * @param properties
	 */
	void process(Driver driver, String url, Properties properties);
}
