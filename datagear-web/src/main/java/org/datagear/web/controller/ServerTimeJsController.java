/*
 * Copyright 2018-present datagear.tech
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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.Global;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 服务端时间控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class ServerTimeJsController extends AbstractController
{
	/**
	 * 服务端日期JS变量名：{@code DATAGEAR_SERVER_TIME}
	 */
	public static final String SERVERTIME_JS_VAR = Global.PRODUCT_NAME_EN_UC + "_SERVER_TIME";

	/** 服务端时间URL */
	public static final String SERVER_TIME_URL = "/static/script/serverTime.js";

	@RequestMapping(SERVER_TIME_URL)
	public void serverTimeJs(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType(CONTENT_TYPE_JAVASCRIPT);

		PrintWriter out = response.getWriter();

		out.println("(function(global)");
		out.println("{");

		out.println("global." + SERVERTIME_JS_VAR + "=" + new java.util.Date().getTime() + ";");

		out.println("})(this);");
	}
}
