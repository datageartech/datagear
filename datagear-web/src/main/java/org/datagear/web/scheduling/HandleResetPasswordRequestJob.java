/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.scheduling;

import java.util.Collection;
import java.util.Date;

import org.datagear.management.domain.ResetPasswordRequest;
import org.datagear.management.domain.ResetPasswordRequestHistory;
import org.datagear.management.service.ResetPasswordRequestHistoryService;
import org.datagear.management.service.ResetPasswordRequestService;
import org.datagear.management.service.UserService;
import org.datagear.web.ResetPasswordRequestConfig;

/**
 * 处理{@linkplain ResetPasswordRequest}任务。
 * 
 * @author datagear@163.com
 *
 */
public class HandleResetPasswordRequestJob
{
	private ResetPasswordRequestService resetPasswordRequestService;

	private ResetPasswordRequestConfig resetPasswordRequestConfig;

	private UserService userService;

	private ResetPasswordRequestHistoryService resetPasswordRequestHistoryService;

	private volatile boolean isHandling = false;

	public HandleResetPasswordRequestJob()
	{
		super();
	}

	public HandleResetPasswordRequestJob(ResetPasswordRequestService resetPasswordRequestService,
			ResetPasswordRequestConfig resetPasswordRequestConfig, UserService userService,
			ResetPasswordRequestHistoryService resetPasswordRequestHistoryService)
	{
		super();
		this.resetPasswordRequestService = resetPasswordRequestService;
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
		this.userService = userService;
		this.resetPasswordRequestHistoryService = resetPasswordRequestHistoryService;
	}

	public ResetPasswordRequestService getResetPasswordRequestService()
	{
		return resetPasswordRequestService;
	}

	public void setResetPasswordRequestService(ResetPasswordRequestService resetPasswordRequestService)
	{
		this.resetPasswordRequestService = resetPasswordRequestService;
	}

	public ResetPasswordRequestConfig getResetPasswordRequestConfig()
	{
		return resetPasswordRequestConfig;
	}

	public void setResetPasswordRequestConfig(ResetPasswordRequestConfig resetPasswordRequestConfig)
	{
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
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
	 * 执行处理。
	 */
	public void handle()
	{
		// 避免任务周期太短，重复处理相同的数据，可能导致ResetPasswordRequestHistory数据库键冲突
		if (this.isHandling)
			return;

		this.isHandling = true;

		try
		{
			Collection<ResetPasswordRequest> resetPasswordRequests = this.resetPasswordRequestService.getAll();

			for (ResetPasswordRequest resetPasswordRequest : resetPasswordRequests)
			{
				Date currentDate = new Date();
				long currentTime = new Date().getTime();

				Date effectiveDate = this.resetPasswordRequestConfig.getEffectiveTime(resetPasswordRequest.getTime());
				long effectiveTime = effectiveDate.getTime();

				if (effectiveTime < currentTime)
				{
					this.userService.updatePasswordById(resetPasswordRequest.getUser().getId(),
							resetPasswordRequest.getPassword(), false);

					this.resetPasswordRequestService.deleteById(resetPasswordRequest.getId());

					ResetPasswordRequestHistory resetPasswordRequestHistory = new ResetPasswordRequestHistory(
							resetPasswordRequest, currentDate, currentDate);

					this.resetPasswordRequestHistoryService.add(resetPasswordRequestHistory);
				}
			}
		}
		finally
		{
			this.isHandling = false;
		}
	}
}
