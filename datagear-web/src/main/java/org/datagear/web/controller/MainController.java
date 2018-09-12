/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 主页控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/main")
public class MainController extends AbstractController
{
	public MainController()
	{
		super();
	}

	/**
	 * 打开主页面。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping
	public String main(HttpServletRequest request, Model model)
	{
		return "/main";
	}
}
