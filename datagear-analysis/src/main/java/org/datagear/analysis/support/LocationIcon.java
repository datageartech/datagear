/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.Icon;
import org.datagear.util.FileUtil;

/**
 * 位置图标。
 * 
 * @author datagear@163.com
 *
 */
public class LocationIcon extends LocationResource implements Icon
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_LOCATION = "location";

	private String type = "";

	private long lastModified = System.currentTimeMillis();

	public LocationIcon()
	{
	}

	public LocationIcon(String location)
	{
		super(location);
		this.type = FileUtil.getExtension(location);
	}

	@Override
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public long getLastModified()
	{
		String location = getLocation();

		if (LocationResource.isFileLocation(location))
		{
			File file = getLocationFile(location);

			return file.lastModified();
		}
		else
			return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return super.getInputStream();
	}
}
