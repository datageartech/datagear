/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
