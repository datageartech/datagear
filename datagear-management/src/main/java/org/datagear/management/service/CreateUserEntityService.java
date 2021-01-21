/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.management.service;

/**
 * 带有创建用户信息的实体服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface CreateUserEntityService
{
	/**
	 * 更新创建用户ID。
	 * 
	 * @param oldUserId
	 * @param newUserId
	 * @return
	 */
	int updateCreateUserId(String oldUserId, String newUserId);

	/**
	 * 删除指定用户ID的所有实体。
	 * 
	 * TODO 有些业务的删除操作并不是仅删除数据库记录那么简单，后续再考虑实现此接口。
	 * 
	 * @param userIds
	 * @return
	 */
	// int deleteByUserId(String... userIds);
}
