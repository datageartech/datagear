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

	/**
	 * 查询指定资源的{@linkplain Authorization}。
	 * 
	 * @param user
	 * @param resource
	 * @param query
	 * @return
	 */
	List<Authorization> queryForResource(User user, String resource, Query query);
}
