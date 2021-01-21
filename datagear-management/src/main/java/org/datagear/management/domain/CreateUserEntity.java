/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

/**
 * 带有创建用户信息的实体。
 * 
 * @author datagear@163.com
 *
 * @param <ID>
 */
public interface CreateUserEntity<ID> extends Entity<ID>
{
	/**
	 * 获取创建用户。
	 * 
	 * @return
	 */
	User getCreateUser();

	/**
	 * 设置创建用户。
	 * 
	 * @param user
	 */
	void setCreateUser(User user);
}
