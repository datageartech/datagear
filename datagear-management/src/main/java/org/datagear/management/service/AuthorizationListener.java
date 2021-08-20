/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;

/**
 * 授权监听器。
 * <p>
 * 主要用于处理授权变更事件，比如清空或者重新加载缓存。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface AuthorizationListener
{
	/**
	 * 指定资源的授权已更新。
	 * <p>
	 * 当直接修改了指定资源的授权信息后，将触发此事件。
	 * </p>
	 * 
	 * @param resourceType
	 * @param resources
	 */
	void authorizationUpdated(String resourceType, String... resources);

	/**
	 * 未知量的资源的权限已更新。
	 * <p>
	 * 数据权限不仅与直接授权相关，还与下列操作相关：
	 * </p>
	 * <ul>
	 * <li>角色（{@linkplain Role}）启用、禁用</li>
	 * <li>用户（{@linkplain User}）绑定、解绑角色</li>
	 * </ul>
	 * <p>
	 * 当上述操作后，将触发此事件。
	 * </p>
	 */
	void permissionUpdated();
}
