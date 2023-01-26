/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
