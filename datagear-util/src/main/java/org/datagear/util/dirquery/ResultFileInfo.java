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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;

/**
 * 查询结果文件信息。
 * 
 * @author datagear@163.com
 *
 */
public class ResultFileInfo extends FileInfo
{
	private static final long serialVersionUID = 1L;

	public static final String FIELD_NAME = "name";

	public static final String FIELD_DISPLAY_NAME = "displayName";

	public static final String FIELD_BYTES = "bytes";

	public static final String FIELD_SIZE = "size";

	public static final String FIELD_LAST_MODIFIED = "lastModified";

	public static final String FIELD_DISPLAY_LAST_MODIFIED = "displayLastModified";

	/** 相对于根目录的完整路径 */
	private String path;

	/** 上次修改时间 */
	private long lastModified = 0;

	/** 上次修改时间显示信息 */
	private String displayLastModified = "";

	public ResultFileInfo()
	{
		super();
	}

	public ResultFileInfo(String path, String name)
	{
		super(name);
		this.path = FileUtil.trimPath(path, FileUtil.PATH_SEPARATOR_SLASH);
	}

	public ResultFileInfo(String path, String name, boolean directory)
	{
		super(name, directory);
		this.path = FileUtil.trimPath(path, FileUtil.PATH_SEPARATOR_SLASH);
		super.setDisplayName(FileUtil.toDisplayPath(name, directory));
	}

	public ResultFileInfo(String path, String name, boolean directory, long bytes, long lastModified)
	{
		super(name, directory, bytes);
		this.path = FileUtil.trimPath(path, FileUtil.PATH_SEPARATOR_SLASH);
		super.setDisplayName(FileUtil.toDisplayPath(name, directory));
		this.lastModified = lastModified;
		this.displayLastModified = toDisplayLastModified(lastModified);
	}

	public ResultFileInfo(String path, String name, boolean directory, long bytes, long lastModified,
			String displayLastModified)
	{
		super(name, directory, bytes);
		this.path = FileUtil.trimPath(path, FileUtil.PATH_SEPARATOR_SLASH);
		super.setDisplayName(FileUtil.toDisplayPath(name, directory));
		this.lastModified = lastModified;
		this.displayLastModified = displayLastModified;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	public String getDisplayLastModified()
	{
		return displayLastModified;
	}

	public void setDisplayLastModified(String displayLastModified)
	{
		this.displayLastModified = displayLastModified;
	}

	/**
	 * 转换为默认的【上次修改时间】显示信息。
	 * 
	 * @param lastModified
	 * @return
	 */
	public static String toDisplayLastModified(long lastModified)
	{
		if (lastModified <= 0)
			return "";

		Instant instant = Instant.ofEpochMilli(lastModified);
		LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		return LAST_MODIFIED_FORMATTER.format(dt);
	}

	public static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
