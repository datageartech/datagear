/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;

/**
 * {@linkplain Authorization}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface AuthorizationService extends EntityService<String, Authorization>
{
	/**
	 * 是否允许授权操作。
	 * 
	 * @param user
	 * @param resourceType
	 * @param resource
	 * @return
	 */
	boolean isAllowAuthorization(User user, String resourceType, String resource);

	/**
	 * 删除。
	 * 
	 * @param resourceType
	 * @param resource
	 * @param ids
	 * @return
	 */
	int deleteByIds(String resourceType, String resource, String... ids);

	/**
	 * 删除资源授权。
	 * 
	 * @param resourceType
	 * @param resources
	 * @return
	 */
	int deleteByResource(String resourceType, String... resources);
}
