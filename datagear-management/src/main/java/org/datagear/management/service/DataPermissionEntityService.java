/*
 * Copyright 2018-2023 datagear.tech
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

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.User;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;

/**
 * 数据权限{@linkplain EntityService}。
 * 
 * @author datagear@163.com
 *
 * @param <ID>
 * @param <T>
 */
public interface DataPermissionEntityService<ID, T extends DataPermissionEntity<ID>>
		extends EntityService<ID, T>, AuthorizationListener
{
	/**
	 * {@linkplain DataPermissionEntity#getDataPermission()}的权限值标识，表明未加载实际权限值
	 */
	int PERMISSION_NOT_LOADED = -9;

	/**
	 * {@linkplain #getPermission(User, Object)}、{@linkplain #getPermissions(User, Object[])}的返回权限值标识，表明指定ID的记录未找到
	 */
	int PERMISSION_NOT_FOUND = -11;

	/**
	 * 获取数据权限资源类型。
	 * 
	 * @return
	 */
	String getResourceType();

	/**
	 * 获取数据权限。
	 * <p>
	 * 返回结果参考{@linkplain Authorization}类的{@code PERMISSION_*}。
	 * </p>
	 * 
	 * @param user
	 * @param id
	 * @return 可能返回{@linkplain #PERMISSION_NOT_FOUND}
	 */
	int getPermission(User user, ID id);

	/**
	 * 获取数据权限。
	 * <p>
	 * 返回结果参考{@linkplain Authorization}类的{@code PERMISSION_*}。
	 * </p>
	 * 
	 * @param user
	 * @param ids
	 * @return 返回元素可能{@linkplain #PERMISSION_NOT_FOUND}
	 */
	int[] getPermissions(User user, ID[] ids);

	/**
	 * 授权添加。
	 * 
	 * @param user
	 * @param entity
	 * @return
	 * @throws PermissionDeniedException
	 */
	void add(User user, T entity) throws PermissionDeniedException;

	/**
	 * 授权更新。
	 * 
	 * @param user
	 *            操作用户
	 * @param entity
	 * @return
	 * @throws PermissionDeniedException
	 */
	boolean update(User user, T entity) throws PermissionDeniedException;

	/**
	 * 授权删除。
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 * @throws PermissionDeniedException
	 */
	boolean deleteById(User user, ID id) throws PermissionDeniedException;

	/**
	 * 授权删除。
	 * 
	 * @param user
	 *            操作用户
	 * @param ids
	 * @throws PermissionDeniedException
	 */
	boolean[] deleteByIds(User user, ID[] ids) throws PermissionDeniedException;

	/**
	 * 授权根据ID获取。
	 * <p>
	 * 记录未找到将返回{@code null}，没有读权限则抛出{@linkplain PermissionDeniedException}。
	 * </p>
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	T getById(User user, ID id) throws PermissionDeniedException;

	/**
	 * 授权根据ID获取，并用于编辑操作。
	 * <p>
	 * 记录未找到将返回{@code null}，没有编辑权限则抛出{@linkplain PermissionDeniedException}。
	 * </p>
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	T getByIdForEdit(User user, ID id) throws PermissionDeniedException;

	/**
	 * 授权查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param query
	 * @return
	 */
	List<T> query(User user, Query query);

	/**
	 * 授权分页查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param pagingQuery
	 * @return
	 */
	PagingData<T> pagingQuery(User user, PagingQuery pagingQuery);

	/**
	 * 授权分页查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param pagingQuery
	 * @param dataFilter
	 * @return
	 */
	PagingData<T> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter);

	/**
	 * 授权根据ID获取。
	 * 
	 * @param user
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	T getByStringId(User user, String id) throws PermissionDeniedException;
}
