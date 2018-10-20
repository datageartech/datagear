/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
