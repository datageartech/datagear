/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.User;

/**
 * {@linkplain User}业务服务接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface UserService extends EntityService<String, User>
{
	/**
	 * 根据用户名获取用户。
	 * 
	 * @param name
	 * @return
	 */
	User getByName(String name);

	/**
	 * 根据ID获取用户，其密码已被清除。
	 * 
	 * @param id
	 * @return
	 */
	User getByIdNoPassword(String id);

	/**
	 * 更新用户密码。
	 * 
	 * @param id
	 * @param newPassword
	 * @param encrypt
	 * @return
	 */
	boolean updatePasswordById(String id, String newPassword, boolean encrypt);
}
