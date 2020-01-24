package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.datagear.analysis.TemplateDashboardWidget;
import org.datagear.util.IOUtil;

/**
 * 直接{@linkplain DashboardWidgetResManager}。
 * <p>
 * 此类仅实现了
 * </p>
 * <p>
 * {@linkplain DashboardWidgetResManager#getDefaultEncoding()}<br>
 * {@linkplain DashboardWidgetResManager#getReader(String, String, String)}
 * </p>
 * <p>
 * 两个接口，并且{@linkplain DashboardWidgetResManager#getReader(String, String, String)}将资源名（第二个参数<code>name</code>）作为资源输入流返回。
 * </p>
 * <p>
 * 此类可以作为{@linkplain TemplateDashboardWidget#getTemplate()}即是模板内容且不包含任何其他资源依赖的{@linkplain TemplateDashboardWidget}的伪资源管理器。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DirectDashboardWidgetResManager extends AbstractDashboardWidgetResManager
{
	public DirectDashboardWidgetResManager()
	{
		super();
	}

	@Override
	public Reader getReader(String id, String name, String encoding) throws IOException
	{
		return IOUtil.getReader(name);
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
	public boolean contains(String id, String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long lastModified(String id, String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String id)
	{
		throw new UnsupportedOperationException();
	}
}
