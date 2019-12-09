/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.Icon;

/**
 * 位置图标。
 * 
 * @author datagear@163.com
 *
 */
public class LocationIcon extends LocationResource implements Icon
{
	private static final long serialVersionUID = 1L;

	public LocationIcon()
	{
	}

	public LocationIcon(String location)
	{
		super(location);
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return super.getInputStream();
	}
}
