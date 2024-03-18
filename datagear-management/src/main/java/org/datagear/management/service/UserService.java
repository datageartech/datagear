/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.service;

import java.util.List;

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
	 * 根据用户名获取用户，其密码已被清除。
	 * 
	 * @param name
	 * @return
	 */
	User getByNameNoPassword(String name);

	/**
	 * 根据ID获取用户，其密码已被清除。
	 * 
	 * @param id
	 * @return
	 */
	User getByIdNoPassword(String id);

	/**
	 * 根据ID获取用户基本信息，参考{@linkplain User#cloneSimple()}。
	 * 
	 * @param id
	 * @return
	 */
	User getByIdSimple(String id);

	/**
	 * 根据用户名获取用户基本信息，参考{@linkplain User#cloneSimple()}。
	 * 
	 * @param name
	 * @return
	 */
	User getByNameSimple(String name);

	/**
	 * 根据ID获取用户基本信息，参考{@linkplain User#cloneSimple()}。
	 * 
	 * @param ids
	 * @param discardNull
	 *            对于未找到的元素，是否丢弃而不是返回{@code null}元素
	 * @return
	 */
	List<User> getByIdsSimple(String[] ids, boolean discardNull);

	/**
	 * 获总计取用户数。
	 * 
	 * @return
	 */
	int getUserCount();

	/**
	 * 更新，但是忽略{@linkplain User#getRoles()}。
	 * 
	 * @param user
	 * @return
	 */
	boolean updateIgnoreRole(User user);

	/**
	 * 更新用户密码。
	 * 
	 * @param id
	 * @param newPassword
	 * @param encrypt
	 * @return
	 */
	boolean updatePasswordById(String id, String newPassword, boolean encrypt);

	/**
	 * 删除用户。
	 * <p>
	 * 注意：此接口仅删除用户本身信息，另参考{@linkplain #deleteByIds(String[], String)}。
	 * </p>
	 */
	@Override
	boolean deleteById(String id);

	/**
	 * 删除用户。
	 * <p>
	 * 注意：此接口仅删除用户本身信息，另参考{@linkplain #deleteByIds(String[], String)}。
	 * </p>
	 */
	@Override
	boolean[] deleteByIds(String[] ids);

	/**
	 * 删除用户，同时将其创建的业务数据都迁移至目标用户。
	 * 
	 * @param ids
	 *            待删除的用户ID
	 * @param migrateToId
	 *            业务数据要迁移到的用户ID
	 */
	void deleteByIds(String[] ids, String migrateToId);
}
