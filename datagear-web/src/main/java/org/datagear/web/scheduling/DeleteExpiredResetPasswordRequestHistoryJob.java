/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.scheduling;

import java.util.Date;

import org.datagear.management.service.ResetPasswordRequestHistoryService;
import org.datagear.web.ResetPasswordRequestConfig;

/**
 * 删除过期重设密码请求历史任务。
 * 
 * @author datagear@163.com
 *
 */
public class DeleteExpiredResetPasswordRequestHistoryJob
{
	private ResetPasswordRequestConfig resetPasswordRequestConfig;

	private ResetPasswordRequestHistoryService resetPasswordRequestHistoryService;

	public DeleteExpiredResetPasswordRequestHistoryJob()
	{
		super();
	}

	public DeleteExpiredResetPasswordRequestHistoryJob(ResetPasswordRequestConfig resetPasswordRequestConfig,
			ResetPasswordRequestHistoryService resetPasswordRequestHistoryService)
	{
		super();
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
		this.resetPasswordRequestHistoryService = resetPasswordRequestHistoryService;
	}

	public ResetPasswordRequestConfig getResetPasswordRequestConfig()
	{
		return resetPasswordRequestConfig;
	}

	public void setResetPasswordRequestConfig(ResetPasswordRequestConfig resetPasswordRequestConfig)
	{
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
	}

	public ResetPasswordRequestHistoryService getResetPasswordRequestHistoryService()
	{
		return resetPasswordRequestHistoryService;
	}

	public void setResetPasswordRequestHistoryService(
			ResetPasswordRequestHistoryService resetPasswordRequestHistoryService)
	{
		this.resetPasswordRequestHistoryService = resetPasswordRequestHistoryService;
	}

	/**
	 * 删除
	 */
	public void delete()
	{
		Date retainDate = this.resetPasswordRequestConfig.getHistoryRetainDate();
		this.resetPasswordRequestHistoryService.deleteBefore(retainDate);
	}
}
