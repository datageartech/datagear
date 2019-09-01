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
	 * 查询指定资源的{@linkplain Authorization}。
	 * 
	 * @param user
	 * @param appointResource
	 * @param query
	 * @return
	 */
	List<Authorization> queryForAppointResource(User user, String appointResource, Query query);
}
