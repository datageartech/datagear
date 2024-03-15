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

package org.datagear.util.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.datagear.util.IOUtil;
import org.springframework.core.io.Resource;

/**
 * 版本更新日志解析器。
 * <p>
 * 用于解析格式如：
 * </p>
 * <p>
 * 
 * <pre>
 * --注释0
 * --v1.0.0
 * XXXXXX
 * XXXXXX
 * 
 * --注释1
 * --v2.0.0
 * XXXXXX
 * XXXXXX
 * </pre>
 * </p>
 * <P>
 * 文件中的版本日志信息。
 * </P>
 * 
 * @author datagear@163.com
 *
 */
public class ChangelogResolver extends AbstractVersionContentReader
{
	/** 注释行开头标识 */
	public static final String COMMENT_LINE_PREFIX = "--";

	/** 版本号注释开头标识 */
	public static final String VERSION_LINE_PREFIX = "--v";

	/**
	 * 更新日志资源
	 */
	private Resource resource;

	private String encoding = ENCODING_UTF8;

	/** 注释行前缀 */
	private String commentLinePrefix = COMMENT_LINE_PREFIX;

	/**
	 * 版本号行前缀，前缀后的此行内容是版本号
	 */
	private String versionLinePrefix = VERSION_LINE_PREFIX;

	public ChangelogResolver()
	{
		super();
	}

	public ChangelogResolver(Resource resource)
	{
		super();
		this.resource = resource;
	}

	public Resource getResource()
	{
		return resource;
	}

	public void setResource(Resource resource)
	{
		this.resource = resource;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public String getCommentLinePrefix()
	{
		return commentLinePrefix;
	}

	public void setCommentLinePrefix(String commentLinePrefix)
	{
		this.commentLinePrefix = commentLinePrefix;
	}

	public String getVersionLinePrefix()
	{
		return versionLinePrefix;
	}

	public void setVersionLinePrefix(String versionLinePrefix)
	{
		this.versionLinePrefix = versionLinePrefix;
	}

	/**
	 * 解析所有版本更新日志，按版本号从大到小排列。
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<VersionContent> resolveAll() throws IOException
	{
		BufferedReader reader = null;

		try
		{
			reader = getResourceBufferedReader();

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
	 * 解析最新的N个版本更新日志，按版本号从大到小排列。。
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
	public VersionContent resolveVersion(Version version) throws IOException
	{
		BufferedReader reader = null;

		try
		{
			reader = getResourceBufferedReader();

			List<VersionContent> versionContents = resolveVersionContents(reader, version, version, true, true);

			return (versionContents == null || versionContents.isEmpty() ? null : versionContents.get(0));
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	protected BufferedReader getResourceBufferedReader() throws IOException
	{
		return new BufferedReader(new InputStreamReader(this.resource.getInputStream(), this.encoding));
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
	protected boolean isCommentLine(String line)
	{
		return line.startsWith(this.commentLinePrefix);
	}

	@Override
	protected boolean isVersionLine(String line)
	{
		return line.startsWith(this.versionLinePrefix);
	}

	@Override
	protected Version resolveVersion(String line)
	{
		int start = line.indexOf(this.versionLinePrefix);

		if (start < 0)
			throw new IllegalArgumentException("[" + line + "] is not version line");

		start = start + this.versionLinePrefix.length();

		String version = line.substring(start, line.length()).trim();

		return Version.valueOf(version);
	}
}
