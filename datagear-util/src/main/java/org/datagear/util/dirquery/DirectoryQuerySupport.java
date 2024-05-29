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

package org.datagear.util.dirquery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.FileUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.query.Order;
import org.datagear.util.query.PagingData;

/**
 * 查询目录内文件支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryQuerySupport
{
	private File directory;

	private boolean queryFileLength = true;

	private boolean queryFileLastModified = true;

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
	
	public boolean isQueryFileLength()
	{
		return queryFileLength;
	}

	public void setQueryFileLength(boolean queryFileLength)
	{
		this.queryFileLength = queryFileLength;
	}

	public boolean isQueryFileLastModified()
	{
		return queryFileLastModified;
	}

	public void setQueryFileLastModified(boolean queryFileLastModified)
	{
		this.queryFileLastModified = queryFileLastModified;
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
	 * 在{@linkplain #getDirectory()}子路径内分页查询。
	 * 
	 * @param query
	 * @param subPath
	 *            允许{@code null}
	 * @return
	 */
	public PagingData<ResultFileInfo> pagingQuery(DirectoryPagingQuery query, String subPath)
	{
		List<ResultFileInfo> fileInfos = query(query, subPath);

		PagingData<ResultFileInfo> re = new PagingData<>(query.getPage(), fileInfos.size(), query.getPageSize());
		re.setItems(fileInfos.subList(re.getStartIndex(), re.getEndIndex()));

		return re;
	}

	/**
	 * 在{@linkplain #getDirectory()}内分页查询。
	 * 
	 * @param query
	 * @return
	 */
	public List<ResultFileInfo> query(DirectoryQuery query)
	{
		return query(query, null);
	}

	/**
	 * 在{@linkplain #getDirectory()}子路径内查询。
	 * 
	 * @param query
	 * @param subPath
	 *            允许{@code null}
	 * @return
	 */
	public List<ResultFileInfo> query(DirectoryQuery query, String subPath)
	{
		File base = this.directory;

		if (!StringUtil.isEmpty(subPath))
			base = FileUtil.getFile(base, subPath);

		if (!base.exists() || !base.isDirectory())
			return Collections.emptyList();

		return queryDirectory(query, base);
	}

	protected List<ResultFileInfo> queryDirectory(DirectoryQuery query, File directory)
	{
		boolean descendent = DirectoryPagingQuery.QUERY_RANGE_DESCENDANT.equalsIgnoreCase(query.getQueryRange());
		List<File> files = new ArrayList<>();

		String keyword = resolveQueryKeyword(query);
		searchInDirectory(directory, keyword, descendent, files);

		List<ResultFileInfo> re = toResultFileInfos(directory, files);
		sortResultFileInfos(re, query.getOrder());

		return re;
	}

	/**
	 * 排序
	 * 
	 * @param resultFileInfos
	 * @param order
	 *            允许{@code null}
	 */
	protected void sortResultFileInfos(List<ResultFileInfo> resultFileInfos, Order order)
	{
		Collections.sort(resultFileInfos, new ResultFileInfoComparator(order));
	}

	protected List<ResultFileInfo> toResultFileInfos(File directory, List<File> subFiles)
	{
		if (subFiles.isEmpty())
			return Collections.emptyList();

		List<ResultFileInfo> re = new ArrayList<>(subFiles.size());

		for (File subFile : subFiles)
		{
			re.add(toResultFileInfo(directory, subFile));
		}

		return re;
	}

	protected ResultFileInfo toResultFileInfo(File directory, File subFile)
	{
		String name = FileUtil.getRelativePath(directory, subFile);
		long length = 0;
		long lastModified = 0;
		
		if(isQueryFileLength())
		{
			length = (subFile.isDirectory() ? 0 : subFile.length());
		}
		
		if(isQueryFileLastModified())
		{
			lastModified = subFile.lastModified();
		}

		return new ResultFileInfo(name, subFile.isDirectory(), length, lastModified);
	}

	/**
	 * 在指定目录内搜索名称匹配的文件。
	 * 
	 * @param directory
	 *            要搜索的目录
	 * @param keyword
	 *            {@code null}、{@code ""}匹配所有
	 * @param descendent
	 * @param result
	 */
	protected void searchInDirectory(File directory, String keyword, boolean descendent, List<File> result)
	{
		File[] children = directory.listFiles();
		
		if(children == null)
			return;
		
		for (File child : children)
		{
			String name = child.getName();

			if (matches(keyword, name))
				result.add(child);

			if (descendent && child.isDirectory())
				searchInDirectory(child, keyword, true, result);
		}
	}

	protected boolean matches(String keyword, String filename)
	{
		return (StringUtil.isEmpty(keyword) || this.asteriskPatternMatcher.matches(keyword, filename));
	}

	protected String resolveQueryKeyword(DirectoryQuery query)
	{
		String keyword = query.getKeyword();

		if (StringUtil.isEmpty(keyword))
			return keyword;
		
		if(!keyword.startsWith(AsteriskPatternMatcher.ASTERISK +"") && !keyword.endsWith(AsteriskPatternMatcher.ASTERISK +""))
		{
			keyword = new StringBuilder().append(AsteriskPatternMatcher.ASTERISK).append(keyword)
					.append(AsteriskPatternMatcher.ASTERISK).toString();
		}

		return keyword;
	}

	public static class ResultFileInfoComparator implements Comparator<ResultFileInfo>
	{
		private Order order;

		public ResultFileInfoComparator()
		{
			super();
		}

		public ResultFileInfoComparator(Order order)
		{
			super();
			this.order = order;
		}

		public Order getOrder()
		{
			return order;
		}

		public void setOrder(Order order)
		{
			this.order = order;
		}

		@Override
		public int compare(ResultFileInfo o1, ResultFileInfo o2)
		{
			String name = (this.order == null ? null : this.order.getName());
			String sortType = (this.order == null ? null : this.order.getType());

			if (StringUtil.isEmpty(sortType))
				sortType = Order.ASC;

			if (StringUtil.isEmpty(name))
				return 0;

			int re = 0;

			if (ResultFileInfo.FIELD_NAME.equalsIgnoreCase(name))
			{
				re = o1.getName().compareToIgnoreCase(o2.getName());
			}
			else if (ResultFileInfo.FIELD_BYTES.equalsIgnoreCase(name))
			{
				re = Long.valueOf(o1.getBytes()).compareTo(o2.getBytes());
			}
			else if (ResultFileInfo.FIELD_LAST_MODIFIED.equalsIgnoreCase(name))
			{
				re = Long.valueOf(o1.getLastModified()).compareTo(o2.getLastModified());
			}

			if (Order.DESC.equalsIgnoreCase(sortType))
				re = 0 - re;

			return re;
		}
	}
}
