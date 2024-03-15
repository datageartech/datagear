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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.datagear.analysis.TplDashboardWidget;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象{@linkplain TplDashboardWidgetResManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractTplDashboardWidgetResManager implements TplDashboardWidgetResManager
{
	public AbstractTplDashboardWidgetResManager()
	{
		super();
	}

	@Override
	public String getDefaultEncoding()
	{
		return Charset.defaultCharset().name();
	}

	@Override
	public Reader getReader(TplDashboardWidget widget, String name) throws IOException
	{
		String encoding = getResourceEncodingWithDefault(widget);
		return getReader(widget.getId(), name, encoding);
	}

	@Override
	public Reader getReader(String id, String name, String encoding) throws IOException
	{
		encoding = getResourceEncodingWithDefault(encoding);
		InputStream in = getInputStream(id, name);
		return IOUtil.getReader(in, encoding);
	}

	@Override
	public Writer getWriter(TplDashboardWidget widget, String name) throws IOException
	{
		String encoding = getResourceEncodingWithDefault(widget);
		return getWriter(widget.getId(), name, encoding);
	}

	@Override
	public Writer getWriter(String id, String name, String encoding) throws IOException
	{
		encoding = getResourceEncodingWithDefault(encoding);

		OutputStream out = getOutputStream(id, name);
		return IOUtil.getWriter(out, encoding);
	}

	protected String getResourceEncodingWithDefault(TplDashboardWidget widget)
	{
		String encoding = widget.getTemplateEncoding();

		if (StringUtil.isEmpty(encoding))
			encoding = getDefaultEncoding();

		return encoding;
	}

	protected String getResourceEncodingWithDefault(String encoding)
	{
		if (StringUtil.isEmpty(encoding))
			encoding = getDefaultEncoding();

		return encoding;
	}
}
