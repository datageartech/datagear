/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.Collection;

import org.datagear.management.domain.ResetPasswordRequest;

/**
 * {@linkplain ResetPasswordRequest}业务服务接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface ResetPasswordRequestService
{
	/**
	 * 获取指定用户ID的{@linkplain ResetPasswordRequest}。
	 * 
	 * @param userId
	 * @return
	 */
	ResetPasswordRequest getByUserId(String userId);

	/**
	 * 获取所有{@linkplain ResetPasswordRequest}。
	 * 
	 * @return
	 */
	Collection<ResetPasswordRequest> getAll();

	/**
	 * 添加{@linkplain ResetPasswordRequest}。
	 * <p>
	 * 如果已存在{@linkplain ResetPasswordRequest#getUserId()}的记录，将返回它，否则，返回{@code null}并添加成功。
	 * </p>
	 * 
	 * @param resetPasswordRequest
	 */
	ResetPasswordRequest addIfNone(ResetPasswordRequest resetPasswordRequest);

	/**
	 * 根据ID删除。
	 * 
	 * @param id
	 * @return
	 */
	boolean deleteById(String id);
}
