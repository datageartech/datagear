/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.Global;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 主页控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class MainController extends AbstractController
{
	@Value("${disableRegister}")
	private boolean disableRegister = false;

	@Autowired
	private ChangelogResolver changelogResolver;

	@Value("${disableDetectNewVersion}")
	private boolean disableDetectNewVersion;

	public MainController()
	{
		super();
	}

	public boolean isDisableRegister()
	{
		return disableRegister;
	}

	public void setDisableRegister(boolean disableRegister)
	{
		this.disableRegister = disableRegister;
	}

	public boolean isDisableDetectNewVersion()
	{
		return disableDetectNewVersion;
	}

	public void setDisableDetectNewVersion(boolean disableDetectNewVersion)
	{
		this.disableDetectNewVersion = disableDetectNewVersion;
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
		request.setAttribute("disableRegister", this.disableRegister);
		request.setAttribute("currentUser", WebUtils.getUser(request, response).cloneNoPassword());
		setDetectNewVersionScriptAttr(request, response, this.disableDetectNewVersion);

		return "/main";
	}

	@RequestMapping("/about")
	public String about(HttpServletRequest request)
	{
		return "/about";
	}

	@RequestMapping("/changelog")
	public String changelog(HttpServletRequest request) throws IOException
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

		request.setAttribute("versionChangelogs", versionChangelogs);

		return "/changelog";
	}

	@RequestMapping("/changelogs")
	public String changelogs(HttpServletRequest request) throws IOException
	{
		List<VersionContent> versionChangelogs = this.changelogResolver.resolveAll();

		request.setAttribute("versionChangelogs", versionChangelogs);
		request.setAttribute("allListed", true);

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
}