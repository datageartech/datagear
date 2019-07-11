/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.datagear.util.IOUtil;
import org.datagear.util.version.AbstractVersionContentReader;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

/**
 * 更新日志解析器。
 * 
 * @author datagear@163.com
 *
 */
public class ChangelogResolver extends AbstractVersionContentReader implements ServletContextAware
{
	public static final String DEFAULT_CHANGELOG_PATH = "/WEB-INF/changelog.txt";

	/** 数据库SQL文件中版本号注释开头标识 */
	public static final String VERSION_LINE_PREFIX = "--v";

	private ServletContext servletContext;

	private String changelogPath = DEFAULT_CHANGELOG_PATH;

	private String changelogEncoding = ENCODING_UTF8;

	public ChangelogResolver()
	{
		super();
	}

	public ChangelogResolver(ServletContext servletContext)
	{
		super();
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	public String getChangelogPath()
	{
		return changelogPath;
	}

	public void setChangelogPath(String changelogPath)
	{
		this.changelogPath = changelogPath;
	}

	public String getChangelogEncoding()
	{
		return changelogEncoding;
	}

	public void setChangelogEncoding(String changelogEncoding)
	{
		this.changelogEncoding = changelogEncoding;
	}

	/**
	 * 解析所有版本更新日志。
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<VersionContent> resolveAll() throws IOException
	{
		BufferedReader reader = null;

		try
		{
			reader = getChangelogBufferedReader();

			List<VersionContent> versionContents = resolveVersionContents(reader, null, null, true, true);

			if (versionContents != null)
			{
				Collections.sort(versionContents);
				Collections.reverse(versionContents);
			}

			return versionContents;
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	/**
	 * 解析最新的N个版本更新日志。
	 * 
	 * @param top
	 * @return
	 * @throws IOException
	 */
	public List<VersionContent> resolveRecents(int top) throws IOException
	{
		List<VersionContent> versionContents = resolveAll();

		if (versionContents.size() > top)
			versionContents = versionContents.subList(0, top);

		return versionContents;
	}

	/**
	 * 解析指定版本的更新日志，没有则返回{@code null}。
	 * 
	 * @param version
	 * @return
	 * @throws IOException
	 */
	public VersionContent resolveChangelog(Version version) throws IOException
	{
		BufferedReader reader = null;

		try
		{
			reader = getChangelogBufferedReader();

			List<VersionContent> versionContents = resolveVersionContents(reader, version, version, true, true);

			return (versionContents == null || versionContents.isEmpty() ? null : versionContents.get(0));
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	protected BufferedReader getChangelogBufferedReader() throws IOException
	{
		ServletContextResource resource = new ServletContextResource(this.servletContext, this.changelogPath);

		if (!resource.exists())
			return null;

		return new BufferedReader(new InputStreamReader(resource.getInputStream(), this.changelogEncoding));
	}

	@Override
	protected void handleVersionContentLine(VersionContent versionContent, List<String> contents, StringBuilder cache,
			String line)
	{
		line = line.trim();

		if (!line.isEmpty())
			contents.add(line);
	}

	@Override
	protected void finishVersionContent(VersionContent versionContent, List<String> contents, StringBuilder cache)
	{
	}

	@Override
	protected boolean isVersionLine(String line)
	{
		return line.startsWith(VERSION_LINE_PREFIX);
	}

	@Override
	protected Version resolveVersion(String line)
	{
		int start = line.indexOf(VERSION_LINE_PREFIX);

		if (start < 0)
			throw new IllegalArgumentException("[" + line + "] is not version line");

		start = start + VERSION_LINE_PREFIX.length();

		String version = line.substring(start, line.length()).trim();

		return Version.valueOf(version);
	}
}
