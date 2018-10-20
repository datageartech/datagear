/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 登录控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/login")
public class LoginController extends AbstractController
{
	public LoginController()
	{
		super();
	}

	/**
	 * 打开登录界面。
	 * 
	 * @return
	 */
	@RequestMapping
	public String login()
	{
		return "/login";
	}

	/**
	 * 将用户名编码为可存储至Cookie的字符串。
	 * 
	 * @param username
	 * @return
	 */
	public static String encodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return URLEncoder.encode(username, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}

	/**
	 * 将{@linkplain #encodeCookieUserName(String)}的用户名解码。
	 * 
	 * @param username
	 * @return
	 */
	public static String decodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return URLDecoder.decode(username, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}
}
