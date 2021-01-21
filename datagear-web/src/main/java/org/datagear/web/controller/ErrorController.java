/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 错误处理控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class ErrorController extends AbstractController
{
	public ErrorController()
	{
		super();
	}

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel)
	{
		OperationMessage operationMessage = getOperationMessageForHttpError(request, response);
		WebUtils.setOperationMessage(request, operationMessage);

		return "/error";
	}
}
