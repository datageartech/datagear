/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import java.util.List;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;
import org.datagear.persistence.Query;

/**
 * {@linkplain Authorization}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface AuthorizationService extends DataPermissionEntityService<String, Authorization>
{
	/**
	 * 删除资源授权。
	 * 
	 * @param resourceType
	 * @param resources
	 * @return
	 */
	int deleteByResource(String resourceType, String... resources);

	/**
	 * 对于支持模式匹配授权的资源，获取指定模式匹配源的权限。
	 * <p>
	 * 返回{@code null}表示无对应的授权。
	 * </p>
	 * 
	 * @param user
	 * @param resourceType
	 * @param patternSource
	 * @return
	 */
	Integer getPermissionForPatternSource(User user, String resourceType, String patternSource);

	/**
	 * 查询指定资源的{@linkplain Authorization}。
	 * 
	 * @param user
	 * @param assignedResource
	 * @param query
	 * @return
	 */
	List<Authorization> queryForAssignedResource(User user, String assignedResource, Query query);
}
