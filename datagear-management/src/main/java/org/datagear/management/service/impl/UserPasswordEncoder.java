/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
