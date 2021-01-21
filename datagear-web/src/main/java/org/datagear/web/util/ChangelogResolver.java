/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.datagear.util.IOUtil;
import org.datagear.util.version.AbstractVersionContentReader;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;

/**
 * 更新日志解析器。
 * <p>
 * 此类解析{@linkplain #CHANGELOG_CLASS_PATH}类路径文件的更新日志。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChangelogResolver extends AbstractVersionContentReader
{
	public static final String CHANGELOG_CLASS_PATH = "org/datagear/web/changelog.txt";

	/** 数据库SQL文件中版本号注释开头标识 */
	public static final String VERSION_LINE_PREFIX = "--v";

	private String changelogEncoding = ENCODING_UTF8;

	public ChangelogResolver()
	{
		super();
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
		InputStream in = getClass().getClassLoader().getResourceAsStream(CHANGELOG_CLASS_PATH);
		return new BufferedReader(new InputStreamReader(in, this.changelogEncoding));
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
