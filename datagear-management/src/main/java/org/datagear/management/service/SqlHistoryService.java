/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

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
	 * 分页查询。
	 * 
	 * @param user
	 * @param pagingQuery
	 * @return
	 */
	PagingData<SqlHistory> pagingQueryByUserId(String userId, PagingQuery pagingQuery);
}
