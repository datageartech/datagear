/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
