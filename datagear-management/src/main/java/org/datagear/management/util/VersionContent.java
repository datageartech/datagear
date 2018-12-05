/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.management.util;

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
		return this.versionStartLine >= 0 && this.contents != null && !this.contents.isEmpty();
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
