/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.List;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;
import org.datagear.model.support.Entity;
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
public interface DataPermissionEntityService<ID, T extends Entity<ID>> extends EntityService<ID, T>
{
	/** 数据权限参数：当前用户，参考commonDataPermissionSqls.xml */
	String DATA_PERMISSION_PARAM_CURRENT_USER = "DP_CURRENT_USER";

	/** 数据权限参数：资源类型，参考commonDataPermissionSqls.xml */
	String DATA_PERMISSION_PARAM_RESOURCE_TYPE = "DP_RESOURCE_TYPE";

	/** 数据权限参数：资源是否支持模式匹配，参考commonDataPermissionSqls.xml */
	String DATA_PERMISSION_PARAM_RESOURCE_SUPPORT_PATTERN = "DP_RESOURCE_SUPPORT_PATTERN";

	/** 数据权限参数：资源是否有创建用户，参考commonDataPermissionSqls.xml */
	String DATA_PERMISSION_PARAM_RESOURCE_HAS_CREATOR = "DP_RESOURCE_HAS_CREATOR";

	/** 数据权限参数：最小读权限值 */
	String DATA_PERMISSION_PARAM_MIN_READ_PERMISSION = "DP_MIN_READ_PERMISSION";

	/** 数据权限参数：最大权限值 */
	String DATA_PERMISSION_PARAM_MAX_PERMISSION = "DP_MAX_PERMISSION";

	/**
	 * 获取数据权限。
	 * <p>
	 * 返回结果参考{@linkplain Authorization}类的{@code PERMISSION_*}。
	 * </p>
	 * 
	 * @param user
	 * @param id
	 * @return
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
	 * @return
	 */
	int[] getPermissions(User user, ID[] ids);

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
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	T getById(User user, ID id) throws PermissionDeniedException;

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
}
