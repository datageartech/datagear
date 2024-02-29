/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.StringUtil;
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
	public static final String STATUS_PARAM_NAME = "status";

	public ErrorController()
	{
		super();
	}

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel)
	{
		Integer paramStatus = getStatusParamValue(request, response, springModel);
		if (paramStatus != null)
		{
			response.setStatus(paramStatus);
		}

		OperationMessage operationMessage = getOptMsgForHttpError(request, response);
		WebUtils.setOperationMessage(request, operationMessage);

		return "/error";
	}

	protected Integer getStatusParamValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel)
	{
		Integer re = null;

		String statusStr = request.getParameter(STATUS_PARAM_NAME);
		if (!StringUtil.isEmpty(statusStr))
		{
			try
			{
				re = Integer.parseInt(statusStr);
			}
			catch (Exception e)
			{
				re = null;
			}
		}

		return re;
	}
}
