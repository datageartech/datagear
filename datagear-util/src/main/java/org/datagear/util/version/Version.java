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

import org.datagear.util.StringUtil;

/**
 * 版本号。
 * <p>
 * 示例：
 * </p>
 * <p>
 * {@code 0.1}、 {@code 1.2}、 {@code 2.05}、 {@code 2.1.3}、 {@code 2.09.03}、
 * {@code 3.2-A0}、 {@code 3.3-B1}、 {@code 3.5.2-B3}
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class Version implements Serializable, Comparable<Version>
{
	public static final Version ZERO_VERSION = new Version(0, 0, 0);

	private static final long serialVersionUID = 1L;

	/** 主版本号 */
	private final int major;

	/** 次版本号 */
	private final int minor;

	/** 修订版本号 */
	private final int revision;

	/** 先行预览版本号 */
	private final String build;

	public Version()
	{
		this(0, 0, 0);
	}

	public Version(int major, int minor, int revision)
	{
		this(major, minor, revision, "");
	}

	public Version(int major, int minor, int revision, String build)
	{
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.build = trimVersionPart(build);
	}

	public Version(String major, String minor, String revision)
	{
		this(major, minor, revision, "");
	}

	public Version(String major, String minor, String revision, String build)
	{
		super();

		major = trimVersionPart(major);
		minor = trimVersionPart(minor);
		revision = trimVersionPart(revision);
		build = trimVersionPart(build);

		if (StringUtil.isEmpty(major))
			major = "0";

		if (StringUtil.isEmpty(minor))
			minor = "0";

		if (StringUtil.isEmpty(revision))
			revision = "0";

		this.major = Integer.parseInt(major);
		this.minor = Integer.parseInt(minor);
		this.revision = Integer.parseInt(revision);
		this.build = build;
	}

	public Version(Version version)
	{
		this(version.major, version.minor, version.revision, version.build);
	}

	public int getMajor()
	{
		return this.major;
	}

	public int getMinor()
	{
		return this.minor;
	}

	public int getRevision()
	{
		return this.revision;
	}

	public String getBuild()
	{
		return build;
	}

	/**
	 * 是否比指定版本号低。
	 * 
	 * @param another
	 * @return
	 */
	public boolean isLowerThan(Version another)
	{
		return (compareTo(another) < 0);
	}

	/**
	 * 是否比指定版本号高。
	 * 
	 * @param another
	 * @return
	 */
	public boolean isHigherThan(Version another)
	{
		return (compareTo(another) > 0);
	}

	/**
	 * 是否相同版本号。
	 * 
	 * @param another
	 * @return
	 */
	public boolean isEqualTo(Version another)
	{
		return (compareTo(another) == 0);
	}

	/**
	 * 获取字符串形式。
	 * 
	 * @return
	 */
	public String stringValue()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.major).append('.').append(this.minor).append('.').append(this.revision);

		if (!this.build.isEmpty())
			sb.append('-').append(this.build);

		return sb.toString();
	}

	@Override
	public int compareTo(Version o)
	{
		int re = this.major - o.major;

		if (re != 0)
			return re;

		re = this.minor - o.minor;

		if (re != 0)
			return re;

		re = this.revision - o.revision;

		if (re != 0)
			return re;

		// 带有先行预览版本号的始终低于不带的
		if (this.build.isEmpty() && !o.build.isEmpty())
			return 1;
		else if (!this.build.isEmpty() && o.build.isEmpty())
			return -1;
		else
			return this.build.compareTo(o.build);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((build == null) ? 0 : build.hashCode());
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + revision;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (build == null)
		{
			if (other.build != null)
				return false;
		}
		else if (!build.equals(other.build))
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (revision != other.revision)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return stringValue();
	}

	protected String trimVersionPart(String part)
	{
		return (part == null ? "" : part.trim());
	}

	/**
	 * 解析{@linkplain Version}。
	 * <p>
	 * 支持格式：{@code 0.1}、 {@code 1.2}、 {@code 2.05}、 {@code 2.1.3}、
	 * {@code 2.09.03}、 {@code 3.2-A0}、 {@code 3.3-B1}、 {@code 3.5.2-B3}
	 * </p>
	 * 
	 * @param version
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Version valueOf(String version) throws IllegalArgumentException
	{
		if (!isValidVersion(version))
			throw new IllegalArgumentException("illegal version : " + version);

		int bidx = version.indexOf('-');

		String p0 = (bidx <= 0 ? version : version.substring(0, bidx));
		String build = (bidx > 0 && bidx < (version.length() - 1) ? version.substring(bidx + 1) : "");

		String[] vs = p0.split("\\.");
		String major = (vs.length > 0 ? vs[0] : null);
		String minor = (vs.length > 1 ? vs[1] : null);
		String revision = (vs.length > 2 ? vs[2] : null);

		return new Version(major, minor, revision, build);
	}

	/**
	 * 复制{@linkplain Version}。
	 * 
	 * @param version
	 * @return
	 */
	public static Version valueOf(Version version)
	{
		return new Version(version);
	}

	/**
	 * 是否是合法的版本号。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isValidVersion(String version)
	{
		return (version != null && version.matches("\\d+(\\.\\d+){1,2}(\\-\\w+){0,1}"));
	}
}
