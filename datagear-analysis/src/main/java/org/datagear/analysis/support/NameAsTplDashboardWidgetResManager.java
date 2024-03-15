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

package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.TplDashboardWidget;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 将资源名称作为资源内容的{@linkplain TplDashboardWidgetResManager}。
 * <p>
 * 此类的：
 * </p>
 * <p>
 * {@linkplain #exists(String, String)}始终返回{@code true}；
 * </p>
 * <p>
 * {@linkplain #getReader(TplDashboardWidget, String)}、{@linkplain #getReader(String, String, String)}始终返回由资源名称构建的输入流；
 * </p>
 * <p>
 * {@linkplain #lastModified(String, String)}始终返回{@code 0}；
 * </p>
 * <p>
 * {@linkplain #list(String)}始终返回空列表；
 * </p>
 * <p>
 * 其他方法直接抛出{@linkplain UnsupportedOperationException}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class NameAsTplDashboardWidgetResManager extends AbstractTplDashboardWidgetResManager
{
	public NameAsTplDashboardWidgetResManager()
	{
		super();
	}

	@Override
	public boolean exists(String id, String name)
	{
		return true;
	}

	@Override
	public Reader getReader(String id, String name, String encoding) throws IOException
	{
		String content = name;
		if (StringUtil.isEmpty(content))
			content = "";

		return IOUtil.getReader(content);
	}

	@Override
	public Writer getWriter(String id, String name, String encoding) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream(String id, String name) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream getOutputStream(String id, String name) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyFrom(String id, File directory) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyTo(String id, File directory) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyTo(String sourceId, String targetId) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long lastModified(String id, String name)
	{
		return 0;
	}

	@Override
	public List<String> list(String id)
	{
		return Collections.emptyList();
	}

	@Override
	public void delete(String id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String id, String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> rename(String id, String srcName, String destName) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
