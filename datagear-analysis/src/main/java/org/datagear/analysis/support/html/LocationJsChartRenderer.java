/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.datagear.analysis.support.LocationResource;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 位置{@linkplain JsChartRenderer}。
 * 
 * @author datagear@163.com
 *
 */
public class LocationJsChartRenderer extends LocationResource implements JsChartRenderer
{
	private static final long serialVersionUID = 1L;

	private String encoding;

	public LocationJsChartRenderer()
	{
		super();
	}

	public LocationJsChartRenderer(String location)
	{
		super(location);
	}

	public boolean hasEncoding()
	{
		return !StringUtil.isEmpty(encoding);
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	public Reader getReader() throws IOException
	{
		InputStream in = getInputStream();
		return IOUtil.getReader(in, this.encoding);
	}
}
