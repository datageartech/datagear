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

/**
 * {@linkplain HtmlChartPlugin}用途枚举定义。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginUsage
{
	/**
	 * 插件用途：正常。
	 * <p>
	 * 表明插件是具有正常渲染图表的功能。
	 * </p>
	 */
	public static final String NORMAL = "normal";

	/**
	 * 插件用途：依赖库。
	 * <p>
	 * 表明插件不具有渲染图表的功能，仅用于提供依赖库。
	 * </p>
	 */
	public static final String LIB = "lib";

	/**
	 * 规范用途。
	 * 
	 * @param type
	 * @return
	 */
	public static String normalize(String type)
	{
		return normalize(type, NORMAL);
	}

	/**
	 * 规范用途。
	 * 
	 * @param type
	 * @param dftType
	 * @return
	 */
	public static String normalize(String type, String dftType)
	{
		if (type == null)
			return dftType;

		if (NORMAL.equalsIgnoreCase(type))
			return NORMAL;

		if (LIB.equalsIgnoreCase(type))
			return LIB;

		return dftType;
	}
}
