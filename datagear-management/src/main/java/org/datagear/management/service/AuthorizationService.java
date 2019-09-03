/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
	 * 对于支持模式匹配授权的资源，是否有权限保存指定模式匹配源的资源。
	 * 
	 * @param user
	 * @param resourceType
	 * @param patternSource
	 * @return
	 */
	boolean canSaveForPatternSource(User user, String resourceType, String patternSource);

	/**
	 * 查询指定资源的{@linkplain Authorization}。
	 * 
	 * @param user
	 * @param appointResource
	 * @param query
	 * @return
	 */
	List<Authorization> queryForAppointResource(User user, String appointResource, Query query);
}
