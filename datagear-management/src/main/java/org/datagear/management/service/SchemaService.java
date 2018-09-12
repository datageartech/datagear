/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service;

import org.datagear.management.domain.Schema;

/**
 * {@linkplain Schema}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SchemaService extends EntityService<String, Schema>
{
	/**
	 * 更新创建用户ID。
	 * 
	 * @param oldUserId
	 * @param newUserId
	 * @return
	 */
	int updateCreateUserId(String oldUserId, String newUserId);

	/**
	 * 删除指定用户ID的{@linkplain Schema}。
	 * 
	 * @param userIds
	 * @return
	 */
	int deleteByUserId(String... userIds);
}
