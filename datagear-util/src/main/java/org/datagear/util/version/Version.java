/*
 * Copyright 2018-2023 datagear.tech
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

/**
 * 版本号。
 * 
 * @author datagear@163.com
 *
 */
public class Version implements Serializable, Comparable<Version>
{
	public static final Version ZERO_VERSION = new Version("0", "0", "0");

	private static final long serialVersionUID = 1L;

	public static final String SPLITTER_D = ".";

	public static final String SPLITTER_D_REGEX = "\\.";

	public static final String SPLITTER_L = "-";

	public static final String SPLITTER_L_REGEX = "\\-";

	public static final String ZERO = "0";

	/** 主版本号 */
	private String major;

	/** 次版本号 */
	private String minor;

	/** 修订版本号 */
	private String revision;

	/** 内部编译版本号 */
	private String build;

	public Version()
	{
		this(ZERO, ZERO, ZERO, "");
	}

	public Version(String major)
	{
		this(major, ZERO, ZERO, "");
	}

	public Version(String major, String minor)
	{
		this(major, minor, ZERO, "");
	}

	public Version(String major, String minor, String revision)
	{
		this(major, minor, revision, "");
	}

	public Version(String major, String minor, String revision, String build)
	{
		super();
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.build = build;

		if (this.major == null || this.major.isEmpty())
			this.major = ZERO;
		if (this.minor == null || this.minor.isEmpty())
			this.minor = ZERO;
		if (this.revision == null || this.revision.isEmpty())
			this.revision = ZERO;
		if (this.build == null)
			this.build = "";
	}

	public Version(Version version)
	{
		this(version.major, version.minor, version.revision, version.build);
	}

	public String getMajor()
	{
		return major;
	}

	public void setMajor(String major)
	{
		this.major = major;

		if (this.major == null || this.major.isEmpty())
			this.major = ZERO;
	}

	public String getMinor()
	{
		return minor;
	}

	public void setMinor(String minor)
	{
		this.minor = minor;

		if (this.minor == null || this.minor.isEmpty())
			this.minor = ZERO;
	}

	public String getRevision()
	{
		return revision;
	}

	public void setRevision(String revision)
	{
		this.revision = revision;

		if (this.revision == null || this.revision.isEmpty())
			this.revision = ZERO;
	}

	public String getBuild()
	{
		return build;
	}

	public void setBuild(String build)
	{
		this.build = build;

		if (this.build == null)
			this.build = "";
	}

	/**
	 * 是否比指定版本号低。
	 * 
	 * @param another
	 * @return
	 */
	public boolean isLowerThan(Version another)
	{
		int ma = compareWithLength(this.major, another.major);

		if (ma < 0)
			return true;
		else if (ma > 0)
			return false;
		else
		{
			int mi = compareWithLength(this.minor, another.minor);

			if (mi < 0)
				return true;
			else if (mi > 0)
				return false;
			else
			{
				int re = compareWithLength(this.revision, another.revision);

				if (re < 0)
					return true;
				else if (re > 0)
					return false;
				else
				{
					int bu = compareWithLength(this.build, another.build);

					if (bu < 0)
						return true;
					else
						return false;
				}
			}
		}
	}

	/**
	 * 是否比指定版本号高。
	 * 
	 * @param another
	 * @return
	 */
	public boolean isHigherThan(Version another)
	{
		return !isLowerThan(another) && !equals(another);
	}

	@Override
	public int compareTo(Version o)
	{
		if (this.isLowerThan(o))
			return -1;
		else if (this.equals(o))
			return 0;
		else
			return 1;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((major == null) ? 0 : major.hashCode());
		result = prime * result + ((minor == null) ? 0 : minor.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Version))
			return false;
		Version other = (Version) obj;
		if (major == null)
		{
			if (other.major != null)
				return false;
		}
		else if (!major.equals(other.major))
			return false;
		if (minor == null)
		{
			if (other.minor != null)
				return false;
		}
		else if (!minor.equals(other.minor))
			return false;
		if (revision == null)
		{
			if (other.revision != null)
				return false;
		}
		else if (!revision.equals(other.revision))
			return false;
		if (build == null)
		{
			if (other.build != null)
				return false;
		}
		else if (!build.equals(other.build))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return stringOf(this);
	}

	/**
	 * 长度优先比较字符串。
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected static int compareWithLength(String a, String b)
	{
		if (a == null)
		{
			if (b == null)
				return 0;
			else
				return -1;
		}
		else
		{
			if (b == null)
				return 1;
			else
			{
				int alen = a.length(), blen = b.length();

				if (alen == blen)
				{
					return a.compareTo(b);
				}
				else
					return alen - blen;
			}
		}
	}

	/**
	 * 返回{@linkplain Version}的字符串形式。
	 * 
	 * @param version
	 * @return
	 */
	public static String stringOf(Version version)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(version.major).append(SPLITTER_D).append(version.minor).append(SPLITTER_D).append(version.revision);

		if (!version.build.isEmpty())
			sb.append(SPLITTER_L).append(version.build);

		return sb.toString();
	}

	/**
	 * 构建Version。
	 * <p>
	 * 1.0、1.0-A1、1.1.0、1.1.0-A1
	 * </p>
	 * 
	 * @param version
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Version valueOf(String version) throws IllegalArgumentException
	{
		if (!isValidVersionString(version))
			throw new IllegalArgumentException("illegal version : " + version);

		String[] vs = version.split(SPLITTER_D_REGEX);

		String major = vs[0];
		String minor = (vs.length > 1 ? vs[1] : null);
		String revision = (vs.length > 2 ? vs[2] : null);
		String build = null;

		if (revision != null)
		{
			String[] bs = revision.split(SPLITTER_L_REGEX);

			revision = bs[0];
			build = (bs.length > 1 ? bs[1] : null);
		}
		else if (minor != null)
		{
			String[] bs = minor.split(SPLITTER_L_REGEX);

			minor = bs[0];
			build = (bs.length > 1 ? bs[1] : null);
		}
		else if (major != null)
		{
			String[] bs = major.split(SPLITTER_L_REGEX);

			major = bs[0];
			build = (bs.length > 1 ? bs[1] : null);
		}

		return new Version(major, minor, revision, build);
	}

	/**
	 * 字符串版本号是否合法。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isValidVersionString(String version)
	{
		return (version != null && version.matches("\\d+(\\.\\d+){1,2}(\\-\\w+){0,1}"));
	}
}
