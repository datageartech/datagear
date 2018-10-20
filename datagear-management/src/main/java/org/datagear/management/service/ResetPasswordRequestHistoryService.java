/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.Date;

import org.datagear.management.domain.ResetPasswordRequestHistory;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;

/**
 * {@linkplain ResetPasswordRequestHistory}业务服务接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface ResetPasswordRequestHistoryService
{
	/**
	 * 添加。
	 * 
	 * @param resetPasswordRequestHistory
	 */
	boolean add(ResetPasswordRequestHistory resetPasswordRequestHistory);

	/**
	 * 获取指定历史。
	 * 
	 * @param resetPasswordRequestId
	 * @return
	 */
	ResetPasswordRequestHistory get(String resetPasswordRequestId);

	/**
	 * 分页查询。
	 * 
	 * @param user
	 * @param pagingQuery
	 * @return
	 */
	PagingData<ResetPasswordRequestHistory> pagingQuery(PagingQuery pagingQuery);

	/**
	 * 删除{@linkplain ResetPasswordRequestHistory#getCreateTime()}在指定日期之前的记录。
	 * 
	 * @param createDate
	 * @return
	 */
	int deleteBefore(Date createDate);
}
