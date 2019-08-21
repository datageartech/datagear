/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.RoleUser;
import org.datagear.management.domain.User;

/**
 * {@linkplain RoleUser}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface RoleUserService extends EntityService<String, RoleUser>
{
	/**
	 * 根据{@linkplain Role}和{@linkplain User}获取记录，没有找到返回{@code null}。
	 * 
	 * @param role
	 * @param user
	 * @return
	 */
	RoleUser getByRoleAndUser(Role role, User user);

	/**
	 * 是否已存在重复记录。
	 * 
	 * @param role
	 * @param user
	 * @return
	 */
	boolean exists(Role role, User user);

	/**
	 * 添加不重复的记录。
	 * 
	 * @param roleUsers
	 * @return
	 */
	boolean[] addIfInexistence(RoleUser... roleUsers);
}
