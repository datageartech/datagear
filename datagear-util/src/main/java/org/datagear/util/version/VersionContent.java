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

package org.datagear.util.version;

import java.io.Serializable;
import java.util.List;

/**
 * 版本内容。
 * 
 * @author datagear@163.com
 *
 */
public class VersionContent implements Serializable, Comparable<VersionContent>
{
	private static final long serialVersionUID = 1L;

	private Version version;

	/** 内容 */
	private List<String> contents;

	/** 版本所在行号 */
	private int versionStartLine;

	/** 版本内容结束行 */
	private int versionEndLine;

	public VersionContent()
	{
		super();
	}

	public VersionContent(Version version)
	{
		super();
		this.version = version;
	}

	public VersionContent(Version version, List<String> contents, int versionStartLine, int versionEndLine)
	{
		super();
		this.version = version;
		this.contents = contents;
		this.versionStartLine = versionStartLine;
		this.versionEndLine = versionEndLine;
	}

	public Version getVersion()
	{
		return version;
	}

	public void setVersion(Version version)
	{
		this.version = version;
	}

	/**
	 * 是否有版本内容。
	 * 
	 * @return
	 */
	public boolean hasContents()
	{
		return this.contents != null && !this.contents.isEmpty();
	}

	public List<String> getContents()
	{
		return contents;
	}

	public void setContents(List<String> contents)
	{
		this.contents = contents;
	}

	public int getVersionStartLine()
	{
		return versionStartLine;
	}

	public void setVersionStartLine(int versionStartLine)
	{
		this.versionStartLine = versionStartLine;
	}

	public int getVersionEndLine()
	{
		return versionEndLine;
	}

	public void setVersionEndLine(int versionEndLine)
	{
		this.versionEndLine = versionEndLine;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [version=" + version + ", contents=" + contents + ", versionStartLine="
				+ versionStartLine + ", versionEndLine=" + versionEndLine + "]";
	}

	@Override
	public int compareTo(VersionContent o)
	{
		if (this.version == null && o.version == null)
			return 0;
		else if (this.version == null)
			return -1;
		else if (o.version == null)
			return 1;
		else
			return this.version.compareTo(o.version);
	}
}
