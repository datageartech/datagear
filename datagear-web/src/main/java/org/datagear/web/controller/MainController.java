/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datagear.management.util.Version;
import org.datagear.management.util.VersionContent;
import org.datagear.web.util.ChangelogResolver;
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
	private String version;

	private boolean disableRegister = false;

	@Autowired
	private ChangelogResolver changelogResolver;

	public MainController()
	{
		super();
	}

	public MainController(String version, ChangelogResolver changelogResolver)
	{
		super();
		this.version = version;
		this.changelogResolver = changelogResolver;
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

	@RequestMapping("/changelog")
	public String changelog(HttpServletRequest request) throws IOException
	{
		Version version = null;

		try
		{
			version = Version.valueOf(this.version);
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

	@RequestMapping(value = "/changeThemeData", produces = CONTENT_TYPE_JSON)
	public String changeThemeData(HttpServletRequest request)
	{
		return "/change_theme_data";
	}
}
