/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
 * 位置{@linkplain ScriptContent}。
 * 
 * @author datagear@163.com
 *
 */
public class LocationScriptContent extends LocationResource implements ScriptContent
{
	private static final long serialVersionUID = 1L;

	private String encoding;

	public LocationScriptContent()
	{
		super();
	}

	public LocationScriptContent(String location)
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
