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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datagear.util.Global;
import org.datagear.util.version.ChangelogResolver;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 更新日志控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class ChangelogController extends AbstractController
{
	@Autowired
	private ChangelogResolver changelogResolver;

	public ChangelogController()
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

	@RequestMapping("/changelog")
	public String changelog(HttpServletRequest request, Model model) throws IOException
	{
		Version version = null;

		try
		{
			version = getLatestVersion();
		}
		catch (Exception e)
		{
		}

		List<VersionContent> versionChangelogs = new ArrayList<>();

		if (version != null)
		{
			VersionContent versionChangelog = this.changelogResolver.resolveVersion(version);
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
	
	protected Version getLatestVersion() throws Exception
	{
		return Version.valueOf(Global.VERSION);
	}
}