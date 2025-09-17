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
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.StringUtil;

/**
 * 版本号匹配模式。
 * <p>
 * 支持的匹配模式如下：
 * <p>
 * {@code null} 表示匹配任意版本
 * </p>
 * <p>
 * {@code ""} 表示匹配任意版本
 * </p>
 * <p>
 * {@code "version"} 表示必须等于{@code version}版本
 * </p>
 * <p>
 * {@code "^version"} 表示主版本号必须相同、且次版本号/修订版本号必须大于等于{@code version}版本
 * </p>
 * <p>
 * {@code "~version"} 表示主版本号/次版本号必须相同、且修订版本号必须大于等于{@code version}版本
 * </p>
 * <p>
 * {@code ">=version"} 表示必须大于或等于{@code version}版本
 * </p>
 * <p>
 * {@code ">version"} 表示必须大于{@code version}版本
 * </p>
 * <p>
 * {@code "<=version"} 表示必须小于或等于{@code version}版本
 * </p>
 * <p>
 * {@code "<version"} 表示必须小于{@code version}版本
 * </p>
 * <p>
 * {@code ">=version1 <version2"}
 * 表示必须大于等于{@code version1}版本、且小于{@code version2}版本
 * </p>
 * <p>
 * {@code ">=version1 <=version2"}
 * 表示必须大于等于{@code version1}版本、且小于等于{@code version2}版本
 * </p>
 * <p>
 * {@code ">version1 <version2"} 表示必须大于{@code version1}版本、且小于{@code version2}版本
 * </p>
 * <p>
 * {@code ">version1 <=version2"}
 * 表示必须大于{@code version1}版本、且小于等于{@code version2}版本
 * </p>
 * <p>
 * {@code version}格式参考{@linkplain Version}类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class VersionPattern
{
	protected static final VersionRange MATCHES_ALL = new VersionRange(null, true, null, true);

	public VersionPattern()
	{
		super();
	}

	/**
	 * 判断是否在版本模式范围内。
	 * 
	 * @param pattern
	 *            允许{@code null}
	 * @param version
	 * @return
	 */
	public boolean matches(String pattern, String version)
	{
		return matches(pattern, Version.valueOf(version));
	}

	/**
	 * 判断是否在版本模式范围内。
	 * 
	 * @param pattern
	 *            允许{@code null}
	 * @param version
	 * @return
	 */
	public boolean matches(String pattern, Version version)
	{
		VersionRange vr = parseVersionRange(pattern);
		return vr.matches(version);
	}

	/**
	 * 获取在版本模式范围内的所有版本。
	 * 
	 * @param pattern
	 *            允许{@code null}
	 * @param versions
	 * @return
	 */
	public List<String> matchesOfString(String pattern, List<String> versions)
	{
		List<String> re = new ArrayList<>(versions.size());

		VersionRange vr = parseVersionRange(pattern);

		for (String version : versions)
		{
			if (vr.matches(Version.valueOf(version)))
				re.add(version);
		}

		return re;
	}

	/**
	 * 获取在版本模式范围内的所有版本。
	 * 
	 * @param pattern
	 *            允许{@code null}
	 * @param versions
	 * @return
	 */
	public List<Version> matchesOfVersion(String pattern, List<Version> versions)
	{
		List<Version> re = new ArrayList<>(versions.size());

		VersionRange vr = parseVersionRange(pattern);

		for (Version version : versions)
		{
			if (vr.matches(version))
				re.add(version);
		}

		return re;
	}

	protected VersionRange parseVersionRange(String pattern)
	{
		if (pattern == null)
			return MATCHES_ALL;

		pattern = pattern.trim();

		if (StringUtil.isEmpty(pattern))
			return MATCHES_ALL;

		VersionRange vr = new VersionRange(null, true, null, true);

		int blankIdx = pattern.indexOf(" ");

		if (blankIdx <= 0)
		{
			inflateVersionRangePart(vr, pattern);
		}
		else
		{
			String part1 = pattern.substring(0, blankIdx);
			String part2 = pattern.substring(blankIdx + 1, pattern.length()).trim();

			inflateVersionRangePart(vr, part1);
			inflateVersionRangePart(vr, part2);
		}

		return vr;
	}

	protected void inflateVersionRangePart(VersionRange vr, String pattern)
	{
		if (StringUtil.isEmpty(pattern))
			return;

		// ^version
		if (pattern.startsWith("^"))
		{
			vr.min = Version.valueOf(pattern.substring(1));
			vr.max = new Version(vr.min.getMajor() + 1, 0, 0);
			vr.includeMin = true;
			vr.includeMax = false;
		}
		// ~version
		else if (pattern.startsWith("~"))
		{
			vr.min = Version.valueOf(pattern.substring(1));
			vr.max = new Version(vr.min.getMajor(), vr.min.getMinor() + 1, 0);
			vr.includeMin = true;
			vr.includeMax = false;
		}
		// >=version
		else if (pattern.startsWith(">="))
		{
			vr.min = Version.valueOf(pattern.substring(2));
			vr.includeMin = true;
		}
		// >version
		else if (pattern.startsWith(">"))
		{
			vr.min = Version.valueOf(pattern.substring(1));
			vr.includeMin = false;
		}
		// <=version
		else if (pattern.startsWith("<="))
		{
			vr.max = Version.valueOf(pattern.substring(2));
			vr.includeMax = true;
		}
		// <version
		else if (pattern.startsWith("<"))
		{
			vr.max = Version.valueOf(pattern.substring(1));
			vr.includeMax = false;
		}
		// version
		else
		{
			vr.min = Version.valueOf(pattern);
			vr.max = vr.min;
			vr.includeMin = true;
			vr.includeMax = true;
		}
	}

	protected static class VersionRange implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Version min = null;
		private boolean includeMin = false;
		private Version max = null;
		private boolean includeMax = false;

		public VersionRange()
		{
			super();
		}

		public VersionRange(Version min, boolean includeMin, Version max, boolean includeMax)
		{
			super();
			this.min = min;
			this.includeMin = includeMin;
			this.max = max;
			this.includeMax = includeMax;
		}

		public boolean matches(Version version)
		{
			int minRe = (this.min == null ? 1 : version.compareTo(this.min));
			int maxRe = (this.max == null ? -1 : version.compareTo(this.max));
			return ((this.includeMin ? minRe >= 0 : minRe > 0) && (this.includeMax ? maxRe <= 0 : maxRe < 0));
		}
	}
}
