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

package org.datagear.web.util.dirquery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.datagear.persistence.PagingData;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.FileUtil;
import org.datagear.util.StringUtil;

/**
 * 查询目录内文件支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryQuerySupport
{
	private File directory;

	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher(true);

	public DirectoryQuerySupport()
	{
		super();
	}

	public DirectoryQuerySupport(File directory)
	{
		super();
		this.directory = directory;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}
	
	public AsteriskPatternMatcher getAsteriskPatternMatcher()
	{
		return asteriskPatternMatcher;
	}

	public void setAsteriskPatternMatcher(AsteriskPatternMatcher asteriskPatternMatcher)
	{
		this.asteriskPatternMatcher = asteriskPatternMatcher;
	}

	/**
	 * 在{@linkplain #getDirectory()}内分页查询。
	 * 
	 * @param query
	 * @return
	 */
	public PagingData<ResultFileInfo> pagingQuery(DirectoryPagingQuery query)
	{
		return pagingQuery(query, null);
	}

	/**
	 * 在子路径内分页查询。
	 * 
	 * @param query
	 * @param subPath
	 *            允许{@code null}
	 * @return
	 */
	public PagingData<ResultFileInfo> pagingQuery(DirectoryPagingQuery query, String subPath)
	{
		File base = this.directory;

		if (!StringUtil.isEmpty(subPath))
			base = FileUtil.getFile(base, subPath);

		String queryRange = query.getQueryRange();

		if (DirectoryPagingQuery.QUERY_RANGE_DESCENDANT.equalsIgnoreCase(queryRange))
		{
			// TODO
		}
		else
		{
			return pagingQueryChildren(query, base);
		}

		return null;
	}

	protected PagingData<ResultFileInfo> pagingQueryChildren(DirectoryPagingQuery query, File file)
	{
		List<File> result = new ArrayList<>();

		searchInDirectory(file, null, false, result);

		// TODO
		return null;
	}

	/**
	 * 在指定目录内搜索名称匹配的文件。
	 * 
	 * @param file
	 *            要搜索的目录，如果不存在或者不是目录，将直接返回
	 * @param keyword
	 *            {@code null}、{@code ""}匹配所有
	 * @param descendent
	 * @param result
	 */
	protected void searchInDirectory(File file, String keyword, boolean descendent, List<File> result)
	{
		if (!file.exists() || !file.isDirectory())
			return;

		File[] children = file.listFiles();
		
		if(children == null)
			return;
		
		for (File child : children)
		{
			String name = child.getName();

			if (StringUtil.isEmpty(keyword) || this.asteriskPatternMatcher.matches(keyword, name))
				result.add(child);

			if (descendent && child.isDirectory())
				searchInDirectory(child, keyword, true, result);
		}
	}
}
