/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.ResetPasswordRequestHistory;
import org.datagear.management.domain.User;
import org.datagear.management.service.ResetPasswordRequestHistoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 重设密码历史控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/resetPasswordRequestHistory")
public class ResetPasswordRequestHistoryController extends AbstractController
{
	@Autowired
	private ResetPasswordRequestHistoryService resetPasswordRequestHistoryService;

	public ResetPasswordRequestHistoryController()
	{
		super();
	}

	public ResetPasswordRequestHistoryController(ResetPasswordRequestHistoryService resetPasswordRequestHistoryService)
	{
		super();
		this.resetPasswordRequestHistoryService = resetPasswordRequestHistoryService;
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

	@RequestMapping
	public String resetPasswordHistory(HttpServletRequest request, org.springframework.ui.Model model)
	{
		User user = new User();

		model.addAttribute("user", user);

		return "/reset_password_request_history";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<ResetPasswordRequestHistory> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final org.springframework.ui.Model springModel) throws Exception
	{
		PagingQuery pagingQuery = getPagingQuery(request, WebUtils.COOKIE_PAGINATION_SIZE);

		PagingData<ResetPasswordRequestHistory> pagingData = this.resetPasswordRequestHistoryService
				.pagingQuery(pagingQuery);

		return pagingData;
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("resetPasswordHistory", code);
	}
}
