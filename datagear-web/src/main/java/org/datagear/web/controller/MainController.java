/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
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
public class MainController extends AbstractController
{
	private String version;

	private boolean disableRegister = false;

	public MainController()
	{
		super();
	}

	public MainController(String version)
	{
		super();
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	@Value("${version}")
	public void setVersion(String version)
	{
		this.version = version;
	}

	public boolean isDisableRegister()
	{
		return disableRegister;
	}

	@Value("${disableRegister}")
	public void setDisableRegister(boolean disableRegister)
	{
		this.disableRegister = disableRegister;
	}

	/**
	 * 打开主页面。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/main")
	public String main(HttpServletRequest request, Model model)
	{
		request.setAttribute("disableRegister", this.disableRegister);

		return "/main";
	}

	@RequestMapping("/about")
	public String about(HttpServletRequest request)
	{
		request.setAttribute("version", this.version);

		return "/about";
	}

	@RequestMapping(value = "/changeThemeData", produces = CONTENT_TYPE_JSON)
	public String changeThemeData(HttpServletRequest request)
	{
		return "/change_theme_data";
	}
}
