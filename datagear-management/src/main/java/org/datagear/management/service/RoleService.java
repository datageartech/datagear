/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import java.util.List;
import java.util.Set;

import org.datagear.management.domain.Role;

/**
 * {@linkplain Role}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface RoleService extends EntityService<String, Role>
{
	@Override
	boolean deleteById(String id) throws DeleteBuiltinRoleDeniedException;

	@Override
	boolean[] deleteByIds(String[] ids) throws DeleteBuiltinRoleDeniedException;

	/**
	 * 查找指定用户的所有角色集。
	 * 
	 * @param userId
	 * @return 没有则返回空集合
	 */
	Set<Role> findByUserId(String userId);

	/**
	 * 获取。
	 * 
	 * @param ids
	 * @return 元素可能为{@code null}，表示未找到对应对象
	 */
	List<Role> getByIds(String... ids);
}
