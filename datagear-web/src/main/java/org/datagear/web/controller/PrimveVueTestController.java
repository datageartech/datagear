/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * PrimveVue测试控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/primevue")
public class PrimveVueTestController extends AbstractController
{
	public static final String KEY_REQUEST_ACTION = "requestAction";
	public static final String REQUEST_ACTION_QUERY = "query";
	public static final String REQUEST_ACTION_SINGLE_SELECT = "singleSelect";
	public static final String REQUEST_ACTION_MULTIPLE_SELECT = "multipleSelect";
	public static final String REQUEST_ACTION_ADD = "add";
	public static final String REQUEST_ACTION_EDIT = "edit";

	public PrimveVueTestController()
	{
		super();
	}

	@RequestMapping({ "", "/" })
	public String main(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/main";
	}

	@RequestMapping("/chartList")
	public String chartList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/chart_table";
	}

	@RequestMapping("/dashboardList")
	public String dashboardList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/dashboard_table";
	}

	@RequestMapping("/addChart")
	public String addChart(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/chart_form";
	}
}