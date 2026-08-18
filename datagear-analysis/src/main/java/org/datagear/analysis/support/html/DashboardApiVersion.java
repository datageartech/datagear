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

package org.datagear.analysis.support.html;

import org.datagear.util.StringUtil;

/**
 * 看板页面端API版本。
 * <p>
 * 系统从{@code 6.0.0}版本起，整个看板页面端API都进行了重构， 为了解决旧版看板的兼容问题，引入了看板API版本概念，
 * 通过在看板里{@code <html>}标签上使用{@code dg-api-version}控制看板API版本切换。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DashboardApiVersion
{
	/**
	 * 版本：{@code 1.0}（5.5.0及以下版本的页面端看板API）
	 */
	public static final String V1 = "1.0";

	/**
	 * 版本：{@code 2.0}（6.0.0及以上版本的页面端看板API）
	 */
	public static final String V2 = "2.0";

	/**
	 * 最新版本
	 */
	public static final String LATEST_VERSION = V2;

	/**
	 * 是否{@code 1.0}版本。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isV1(String version)
	{
		return V1.equals(version);
	}

	/**
	 * 是否{@code 2.0}版本。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isV2(String version)
	{
		return V2.equals(version);
	}

	/**
	 * 转换为合法版本号
	 * 
	 * @param version
	 * @return 只可能是{@linkplain #V1}、{@linkplain #V2}
	 */
	public static String toValidVersion(String version)
	{
		// 默认应返回最新版
		String latestVersion = LATEST_VERSION;
		return normalize(version, latestVersion);
	}

	/**
	 * 修整版本号。
	 * 
	 * @param version
	 * @return
	 */
	public static String trimVersion(String version)
	{
		return normalize(version, version);
	}

	/**
	 * 规范版本号。
	 * 
	 * @param version
	 * @param dftVersion
	 * @return
	 */
	public static String normalize(String version, String dftVersion)
	{
		version = (version == null ? null : version.trim());

		// 为空时必须设为V1，以兼容旧版无API版本的情况
		if (StringUtil.isEmpty(version))
			return V1;

		if (V1.equalsIgnoreCase(version))
			return V1;

		if (V2.equalsIgnoreCase(version))
			return V2;

		return dftVersion;
	}

	/**
	 * 是否有效的版本号。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isValidVersion(String version)
	{
		return (isV1(version) || isV2(version));
	}
}
