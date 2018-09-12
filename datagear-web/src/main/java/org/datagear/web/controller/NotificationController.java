/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.GlobalSetting;
import org.datagear.management.domain.ResetPasswordRequest;
import org.datagear.management.domain.User;
import org.datagear.management.service.ResetPasswordRequestService;
import org.datagear.web.ResetPasswordRequestConfig;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 通知控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/notification")
public class NotificationController extends AbstractController
{
	@Autowired
	private ResetPasswordRequestService resetPasswordRequestService;

	@Autowired
	private ResetPasswordRequestConfig resetPasswordRequestConfig;

	public NotificationController()
	{
		super();
	}

	public NotificationController(ResetPasswordRequestService resetPasswordRequestService,
			ResetPasswordRequestConfig resetPasswordRequestConfig)
	{
		super();
		this.resetPasswordRequestService = resetPasswordRequestService;
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
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

	@RequestMapping(value = "/list", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Notification> list(HttpServletRequest request, HttpServletResponse response,
			GlobalSetting globalSetting)
	{
		List<Notification> notifications = new ArrayList<Notification>();

		Notification notification = getResetPasswordRequestForAdminNotification(request);

		if (notification != null)
			notifications.add(notification);

		return notifications;
	}

	protected Notification getResetPasswordRequestForAdminNotification(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		ResetPasswordRequest resetPasswordRequest = this.resetPasswordRequestService.getByUserId(User.ADMIN_USER_ID);

		if (resetPasswordRequest == null)
			return null;

		if (resetPasswordRequest.getId().equals(session.getAttribute("read.ResetPasswordRequest.id")))
			return null;

		session.setAttribute("read.ResetPasswordRequest.id", resetPasswordRequest.getId());

		SqlTimestampFormatter dateFormatter = new SqlTimestampFormatter();

		String reqTimeStr = dateFormatter.print(new java.sql.Timestamp(resetPasswordRequest.getTime().getTime()),
				WebUtils.getLocale(request));
		String effectiveTimeStr = dateFormatter.print(
				new java.sql.Timestamp(
						this.resetPasswordRequestConfig.getEffectiveTime(resetPasswordRequest.getTime()).getTime()),
				WebUtils.getLocale(request));

		String message = getMessage(request, buildMessageCode("resetPasswordRequest"),
				resetPasswordRequest.getPrincipal(), reqTimeStr, User.ADMIN_USER_ID, effectiveTimeStr);

		return new Notification(message);
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("notification", code);
	}
}
