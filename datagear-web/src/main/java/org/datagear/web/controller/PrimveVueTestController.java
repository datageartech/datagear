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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
	public static final String REQUEST_ACTION_SAVE_ADD = "saveAdd";
	public static final String REQUEST_ACTION_SAVE_EDIT = "saveEdit";
	public static final String REQUEST_ACTION_SAVE = "save";
	public static final String REQUEST_ACTION_VIEW = "view";

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

	@RequestMapping("/addChart")
	public String addChart(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/primevueTest/chart_form";
	}

	@RequestMapping(value = "/saveChart", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveChart(HttpServletRequest request)
	{
		return optMsgSaveSuccessResponseEntity(request);
	}
}