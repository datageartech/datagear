/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.List;

import org.datagear.management.domain.SqlHistory;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;

/**
 * {@linkplain SqlHistory}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SqlHistoryService extends EntityService<String, SqlHistory>
{
	/**
	 * 添加{@linkplain SqlHistory}，并删除过期历史。
	 * 
	 * @param schemaId
	 * @param userId
	 * @param sqls
	 */
	void addForRemain(String schemaId, String userId, List<String> sqls);

	/**
	 * 分页查询。
	 * 
	 * @param schemaId
	 * @param userId
	 * @param pagingQuery
	 * @return
	 */
	PagingData<SqlHistory> pagingQueryByUserId(String schemaId, String userId, PagingQuery pagingQuery);
}
