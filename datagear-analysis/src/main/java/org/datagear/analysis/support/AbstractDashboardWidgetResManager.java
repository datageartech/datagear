package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象{@linkplain DashboardWidgetResManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDashboardWidgetResManager implements DashboardWidgetResManager
{
	public AbstractDashboardWidgetResManager()
	{
		super();
	}

	@Override
	public String getDefaultEncoding()
	{
		return Charset.defaultCharset().name();
	}

	@Override
	public Reader getReader(String id, String name, String encoding) throws IOException
	{
		if (StringUtil.isEmpty(encoding))
			encoding = getDefaultEncoding();

		InputStream in = getInputStream(id, name);
		return IOUtil.getReader(in, encoding);
	}

	@Override
	public Writer getWriter(String id, String name, String encoding) throws IOException
	{
		if (StringUtil.isEmpty(encoding))
			encoding = getDefaultEncoding();

		OutputStream out = getOutputStream(id, name);
		return IOUtil.getWriter(out, encoding);
	}
}
