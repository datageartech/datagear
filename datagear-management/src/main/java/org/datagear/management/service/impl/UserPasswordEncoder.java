/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service.impl;

/**
 * 用户密码加密接口。
 * 
 * @author datagear@163.com
 *
 */
public interface UserPasswordEncoder
{
	/**
	 * 加密密码。
	 * 
	 * @param rawPassword
	 * @return
	 */
	String encode(String rawPassword);
}
