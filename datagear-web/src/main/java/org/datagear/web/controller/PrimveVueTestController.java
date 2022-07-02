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
	public PrimveVueTestController()
	{
		super();
	}

	@RequestMapping({ "", "/" })
	public String main(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/main";
	}

	@RequestMapping("/dataSourceList")
	public String dataSourceList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/dataSource_list";
	}

	@RequestMapping("/projectList")
	public String projectList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/project_list";
	}

	@RequestMapping("/dataSetList")
	public String dataSetList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/dataSet_list";
	}

	@RequestMapping("/chartList")
	public String chartList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/chart_list";
	}

	@RequestMapping("/dashboardList")
	public String dashboardList(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/dashboard_list";
	}

	@RequestMapping("/addChart")
	public String addChart(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/chart_form";
	}
}