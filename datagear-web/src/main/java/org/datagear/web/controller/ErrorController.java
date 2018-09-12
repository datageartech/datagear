/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;

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
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request)
	{
		return "/error";
	}
}
