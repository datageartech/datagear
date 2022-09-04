/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.Global;
import org.datagear.util.IOUtil;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

/**
 * 主页控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class MainController extends AbstractController
{
	public static final String FAVICON_CLASS_PATH = "org/datagear/web/static/image/favicon.ico";

	@Autowired
	private ChangelogResolver changelogResolver;

	public MainController()
	{
		super();
	}

	public ChangelogResolver getChangelogResolver()
	{
		return changelogResolver;
	}

	public void setChangelogResolver(ChangelogResolver changelogResolver)
	{
		this.changelogResolver = changelogResolver;
	}

	/**
	 * 打开主页面。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping({ "", "/", "/index.html" })
	public String main(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		WebUtils.setEnableDetectNewVersionRequest(request);
		
		return "/main";
	}

	@RequestMapping("/about")
	public String about(HttpServletRequest request)
	{
		return "/about";
	}

	@RequestMapping("/changelog")
	public String changelog(HttpServletRequest request, Model model) throws IOException
	{
		Version version = null;

		try
		{
			version = Version.valueOf(Global.VERSION);
		}
		catch (IllegalArgumentException e)
		{
		}

		List<VersionContent> versionChangelogs = new ArrayList<>();

		if (version != null)
		{
			VersionContent versionChangelog = this.changelogResolver.resolveChangelog(version);
			versionChangelogs.add(versionChangelog);
		}

		model.addAttribute("versionChangelogs", versionChangelogs);

		return "/changelog";
	}

	@RequestMapping("/changelogs")
	public String changelogs(HttpServletRequest request, Model model) throws IOException
	{
		List<VersionContent> versionChangelogs = this.changelogResolver.resolveAll();

		model.addAttribute("versionChangelogs", versionChangelogs);
		model.addAttribute("allListed", true);

		return "/changelog";
	}

	@RequestMapping(value = "/changeThemeData")
	public String changeThemeData(HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType(CONTENT_TYPE_JSON);

		return "/change_theme_data";
	}

	@RequestMapping(value = "/changeLocale", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Object changeLocale(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("status", "ok");
		return map;
	}
	
	@RequestMapping("/favicon.ico")
	public void favicon(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest) throws IOException
	{
		ClassPathResource res = new ClassPathResource(FAVICON_CLASS_PATH, getClass().getClassLoader());
		
		long lastModified = 0;
		try
		{
			lastModified = res.lastModified();
		}
		catch(Throwable t)
		{
			lastModified = CONTROLLER_LOAD_TIME;
		}
		
		if (webRequest.checkNotModified(lastModified))
			return;

		response.setContentType("image/x-icon");
		setCacheControlNoCache(response);

		OutputStream out = response.getOutputStream();
		InputStream in = null;
		
		try
		{
			in = res.getInputStream();
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
		}
	}
}