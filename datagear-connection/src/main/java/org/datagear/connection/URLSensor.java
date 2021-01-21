/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection;

/**
 * JDBC连接URL敏感器。
 * 
 * @author datagear@163.com
 *
 */
public interface URLSensor
{
	/**
	 * 是否支持指定JDBC连接url。
	 * 
	 * @param url
	 * @return
	 */
	boolean supports(String url);
}
