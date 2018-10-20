/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import org.datagear.model.support.Entity;

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
