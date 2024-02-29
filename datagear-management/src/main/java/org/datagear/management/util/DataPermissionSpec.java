/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.management.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.util.StringUtil;

/**
 * 数据权限规范。
 * 
 * @author datagear@163.com
 *
 */
public class DataPermissionSpec
{
	/** 数据权限参数：当前用户，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_CURRENT_USER = "DP_CURRENT_USER";

	/** 数据权限参数：当前用户的角色ID集合，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_ROLE_IDS = "DP_ROLE_IDS";

	/** 数据权限参数：资源类型，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_RESOURCE_TYPE = "DP_RESOURCE_TYPE";

	/** 数据权限参数：资源是否有创建用户，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_RESOURCE_HAS_CREATOR = "DP_RESOURCE_HAS_CREATOR";

	/** 数据权限参数：最小读权限值，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_MIN_READ_PERMISSION = "DP_MIN_READ_PERMISSION";

	/** 数据权限参数：最大权限值，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_MAX_PERMISSION = "DP_MAX_PERMISSION";

	/** 数据权限参数：未设置任何权限时的默认权限值，参考：{@code commonDataPermissionSqls.xml} */
	public static final String PARAM_UNSET_PERMISSION = "DP_UNSET_PERMISSION";

	/**
	 * 数据过滤值参数。参考：{@code commonDataPermissionSqls.xml}
	 */
	public static final String PARAM_DATA_FILTER = "DP_DATA_FILTER";

	/** 查询过滤值：我的。参考：{@code commonDataPermissionSqls.xml} */
	public static final String DATA_FILTER_VALUE_MINE = "mine";

	/** 查询过滤值：其他人的。参考：{@code commonDataPermissionSqls.xml} */
	public static final String DATA_FILTER_VALUE_OTHER = "other";

	/** 查询过滤值：全部。参考：{@code commonDataPermissionSqls.xml} */
	public static final String DATA_FILTER_VALUE_ALL = "all";

	public DataPermissionSpec()
	{
		super();
	}

	/**
	 * 设置数据权限查询SQL参数，详细参考：{@code commonDataPermissionSqls.xml}。
	 * 
	 * @param params
	 * @param user
	 * @param resourceType
	 * @param resourceHasCreator
	 */
	public void setParams(Map<String, Object> params, User user, String resourceType, boolean resourceHasCreator)
	{
		params.put(PARAM_CURRENT_USER, user);
		setRoleIdsParam(params, user);
		params.put(PARAM_RESOURCE_TYPE, resourceType);
		params.put(PARAM_RESOURCE_HAS_CREATOR, resourceHasCreator);
		params.put(PARAM_MIN_READ_PERMISSION, Authorization.PERMISSION_READ_START);
		params.put(PARAM_MAX_PERMISSION, Authorization.PERMISSION_MAX);
		params.put(PARAM_UNSET_PERMISSION, Authorization.PERMISSION_NONE_START);
	}

	/**
	 * 设置角色ID集参数。
	 * <p>
	 * 通过SQL实现此逻辑有点复杂，而且可能影响查询性能， 因此在这里以编程方式实现，可以提高数据权限查询性能。
	 * </p>
	 * 
	 * @param params
	 * @param user
	 */
	protected void setRoleIdsParam(Map<String, Object> params, User user)
	{
		Set<Role> roles = user.getRoles();

		if (roles == null || roles.isEmpty())
			return;

		List<String> roleIds = new ArrayList<>(roles.size());

		for (Role role : roles)
		{
			// 必须是启用的
			if (role.isEnabled())
			{
				roleIds.add(role.getId());
			}
		}

		// 为空置为null，简化SQL层判断逻辑
		if (roleIds.isEmpty())
			roleIds = null;

		params.put(PARAM_ROLE_IDS, roleIds);
	}

	/**
	 * 获取当前用户参数。
	 * 
	 * @param params
	 * @return
	 */
	public User getParamCurrentUser(Map<String, Object> params)
	{
		return (User) params.get(PARAM_CURRENT_USER);
	}

	/**
	 * 转换为标准规范的数据过滤值。
	 * 
	 * @param dataFilter
	 *            允许{@code null}
	 * @return
	 */
	public String stdDataFilter(String dataFilter)
	{
		if (DATA_FILTER_VALUE_MINE.equalsIgnoreCase(dataFilter))
			return DATA_FILTER_VALUE_MINE;
		else if (DATA_FILTER_VALUE_OTHER.equalsIgnoreCase(dataFilter))
			return DATA_FILTER_VALUE_OTHER;
		else if (DATA_FILTER_VALUE_ALL.equalsIgnoreCase(dataFilter))
			return DATA_FILTER_VALUE_ALL;
		else
			return DATA_FILTER_VALUE_ALL;
	}

	/**
	 * 设置数据过滤值参数，详细参考：{@code commonDataPermissionSqls.xml}。
	 * 
	 * @param params
	 * @param dataFilter
	 */
	public void setFilterParam(Map<String, Object> params, String dataFilter)
	{
		if (StringUtil.isEmpty(dataFilter))
			dataFilter = null;
		else
			dataFilter = stdDataFilter(dataFilter);

		params.put(PARAM_DATA_FILTER, dataFilter);
	}
}
