/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
public interface DataPermissionEntityService<ID, T extends DataPermissionEntity<ID>> extends EntityService<ID, T>
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

	/** 数据权限参数：未设置任何权限时的默认权限值 */
	String DATA_PERMISSION_PARAM_UNSET_PERMISSION = "DP_UNSET_PERMISSION";

	/** 查询过滤值：我的 */
	String DATA_FILTER_VALUE_MINE = "mine";

	/** 查询过滤值：其他人的 */
	String DATA_FILTER_VALUE_OTHER = "other";

	/** 查询过滤值：全部 */
	String DATA_FILTER_VALUE_ALL = "all";

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
	boolean add(User user, T entity) throws PermissionDeniedException;

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
	 * 授权根据ID获取。
	 * 
	 * @param user
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	T getByStringId(User user, String id) throws PermissionDeniedException;

	/**
	 * 授权根据ID获取，并用于编辑操作。
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
}
