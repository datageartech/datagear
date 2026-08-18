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

package org.datagear.analysis.support;

import java.util.regex.Pattern;

import org.datagear.util.StringUtil;

/**
 * 图表插件ID规范。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginIdSpec
{
	/** 最大长度 */
	public static final int MAX_LENGTH = 100;

	/**
	 * 只允许包含字符：{@code a-z}、{@code A-Z}、{@code 0-9}、{@code .}（点）、{@code _}（下划线）、{@code -}（中划线）。
	 */
	public static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\_\\-]*$");

	public ChartPluginIdSpec()
	{
		super();
	}

	/**
	 * 是否合法图表插件ID。
	 * 
	 * @param id
	 * @return
	 */
	public boolean isValidId(String id)
	{
		if (StringUtil.isEmpty(id))
			return false;

		if (id.length() > MAX_LENGTH)
			return false;

		if (!PATTERN.matcher(id).matches())
			return false;

		return true;
	}
}
