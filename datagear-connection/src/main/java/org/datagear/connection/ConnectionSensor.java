/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.sql.Connection;

/**
 * {@linkplain Connection}敏感器。
 * 
 * @author datagear@163.com
 *
 */
public interface ConnectionSensor
{
	/**
	 * 是否支持指定{@link Connection}。
	 * 
	 * @param cn
	 * @return
	 */
	boolean supports(Connection cn);
}
