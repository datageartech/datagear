/*
 * Copyright 2018-2023 datagear.tech
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
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.IOUtil;

/**
 * 抽象版本内容读取器。
 * <p>
 * 它从符合给定格式的输入流中读取版本内容。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractVersionContentReader
{
	public static final String LINE_SEPARATOR = IOUtil.LINE_SEPARATOR;

	/** UTF-8编码 */
	public static final String ENCODING_UTF8 = "UTF-8";

	public AbstractVersionContentReader()
	{
		super();
	}

	/**
	 * 解析{@linkplain VersionContent}列表。
	 * 
	 * @param reader
	 * @param from
	 *            起始版本，为{@code null}表示从第一个版本
	 * @param to
	 *            结束版本，为{@code null}表示至最后一个版本
	 * @param containsFrom
	 *            是否包含起始版本
	 * @param contailsTo
	 *            是否包含结束版本
	 * @return
	 * @throws IOException
	 */
	protected List<VersionContent> resolveVersionContents(BufferedReader reader, Version from, Version to,
			boolean containsFrom, boolean contailsTo) throws IOException
	{
		List<VersionContent> versionContents = new ArrayList<VersionContent>();

		VersionContent versionContent = null;
		List<String> contents = null;

		StringBuilder lineCache = new StringBuilder();

		String line = null;
		int lineNumber = 0;
		while ((line = reader.readLine()) != null)
		{
			if (isVersionLine(line))
			{
				Version myVersion = resolveVersion(line);

				// 使用方法参数对象调用比较方法，因为它们可能是Version的子类
				if (to != null && (to.isLowerThan(myVersion) || (!contailsTo && to.equals(myVersion))))
				{
					break;
				}

				// 使用方法参数对象调用比较方法，因为它们可能是Version的子类
				if (from == null || from.isLowerThan(myVersion) || (containsFrom && from.equals(myVersion)))
				{
					if (versionContent != null)
					{
						finishVersionContent(versionContent, contents, lineCache);

						versionContent.setVersionEndLine(lineNumber - 1);
						versionContents.add(versionContent);

						versionContent = null;
					}

					if (versionContent == null)
					{
						versionContent = new VersionContent(myVersion);
						contents = new ArrayList<String>();

						versionContent.setContents(contents);
						versionContent.setVersionStartLine(lineNumber);
					}
				}
			}
			else if (isCommentLine(line))
			{

			}
			else if (versionContent != null)
			{
				handleVersionContentLine(versionContent, contents, lineCache, line);
			}
			else
				;

			lineNumber++;
		}

		if (versionContent != null)
		{
			finishVersionContent(versionContent, contents, lineCache);

			versionContent.setVersionEndLine(lineNumber);
			versionContents.add(versionContent);

			versionContent = null;
		}

		return versionContents;
	}

	/**
	 * 处理版本内容行。
	 * 
	 * @param versionContent
	 *            行所处的{@linkplain VersionContent}
	 * @param contents
	 *            行所处的{@linkplain VersionContent#getContents()}
	 * @param cache
	 *            用于对行进行缓存处理的字符串构建器
	 * @param line
	 *            行字符串，原始行内容，未做任何处理
	 */
	protected abstract void handleVersionContentLine(VersionContent versionContent, List<String> contents,
			StringBuilder cache, String line);

	/**
	 * 完成版本内容。
	 * 
	 * @param versionContent
	 * @param contents
	 * @param cache
	 */
	protected abstract void finishVersionContent(VersionContent versionContent, List<String> contents,
			StringBuilder cache);

	/**
	 * 是否是注释行。
	 * 
	 * @param line
	 * @return
	 */
	protected boolean isCommentLine(String line)
	{
		return line.startsWith("--");
	}

	/**
	 * 从指定字符串的{@code prefix}、{@code suffix}之间解析版本号。
	 * 
	 * @param str
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	protected Version resolveVersion(String str, String prefix, String suffix)
	{
		int start = str.indexOf(prefix);

		if (start < 0)
			throw new IllegalArgumentException("[" + str + "] is not version line");

		start = start + prefix.length();
		int end = str.indexOf(suffix, start);

		String version = str.substring(start, end);

		return Version.valueOf(version);
	}

	/**
	 * 判断给定行是否是版本标识行。
	 * 
	 * @param line
	 * @return
	 */
	protected abstract boolean isVersionLine(String line);

	/**
	 * 从行字符串中解析版本号。
	 * 
	 * @param line
	 * @return
	 */
	protected abstract Version resolveVersion(String line);
}
