/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
	 * @param properties
	 */
	void process(Driver driver, Properties properties);
}
