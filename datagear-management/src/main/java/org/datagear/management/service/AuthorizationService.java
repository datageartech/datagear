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
