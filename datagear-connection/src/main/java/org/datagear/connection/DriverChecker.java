/*
 * Copyright (c) 2018 by datagear.org.
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
