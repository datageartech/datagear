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

package org.datagear.web.analysis;

import org.datagear.util.StringUtil;

/**
 * 看板API版本。
 * <p>
 * 从系统{@code 6.0.0}版本起，整个JS端的看板API都进行了重构， 为了解决旧版看板的兼容问题，引入了看板API版本概念，
 * 通过在看板里{@code <html>}标签上使用{@code dg-api-version}控制看板API版本切换。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DashboardApiVersion
{
	/**
	 * 版本：{@code 1.0}（5.5.0及以下版本的JS端看板API）
	 */
	public static final String V1 = "1.0";

	/**
	 * 版本：{@code 2.0}（6.0.0及以上版本的JS端看板API）
	 */
	public static final String V2 = "2.0";

	/**
	 * 是否{@code 1.0}版本。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isV1(String version)
	{
		if (V1.equals(version))
			return true;
		// 为空应判断为V1版本，以兼容旧版没有看板API版本的概念
		else if (StringUtil.isEmpty(version))
			return true;
		else
			return false;
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
}
