/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	private boolean disableRegister = false;

	@Autowired
	private ChangelogResolver changelogResolver;

	private String detectNewVersionScriptLocation;

	public MainController()
	{
		super();
	}

	public MainController(ChangelogResolver changelogResolver)
	{
		super();
		this.changelogResolver = changelogResolver;
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

	public String getDetectNewVersionScriptLocation()
	{
		return detectNewVersionScriptLocation;
	}

	@Value("${detectNewVersionScriptLocation}")
	public void setDetectNewVersionScriptLocation(String detectNewVersionScriptLocation)
	{
		this.detectNewVersionScriptLocation = detectNewVersionScriptLocation;
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
		request.setAttribute("currentUser", User.copyWithoutPassword(WebUtils.getUser(request, response)));
		request.setAttribute("currentVersion", Global.VERSION);
		request.setAttribute("detectNewVersionScript", resolveDetectNewVersionScript(request, response));

		return "/main";
	}

	@RequestMapping("/about")
	public String about(HttpServletRequest request)
	{
		request.setAttribute("version", Global.VERSION);

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

		List<VersionContent> versionChangelogs = new ArrayList<VersionContent>();

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

	protected String resolveDetectNewVersionScript(HttpServletRequest request, HttpServletResponse response)
	{
		if (StringUtil.isEmpty(this.detectNewVersionScriptLocation))
			return "";

		return "<script src=\"" + this.detectNewVersionScriptLocation + "\" type=\"text/javascript\"></script>";
	}
}