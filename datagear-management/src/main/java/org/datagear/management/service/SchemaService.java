/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.Schema;

/**
 * {@linkplain Schema}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SchemaService extends DataPermissionEntityService<String, Schema>, CreateUserEntityService
{
	/**
	 * 删除指定用户ID的{@linkplain Schema}。
	 * 
	 * @param userIds
	 * @return
	 */
	int deleteByUserId(String... userIds);
}
