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

package org.datagear.analysis;

import org.datagear.analysis.support.ResultJsonRule;

/**
 * {@linkplain ResultJsonRule}工具类。
 * 
 * @author datagear@163.com
 *
 */
public class ResultJsonRuleUtil
{
	/**
	 * 获取{@linkplain ResultJsonRule#getDataJsonPath()}。
	 * 
	 * @param jsonRule
	 *            允许{@code null}
	 * @return
	 */
	public static String getResultDataJsonPath(ResultJsonRule jsonRule)
	{
		return (jsonRule == null ? null : jsonRule.getDataJsonPath());
	}

	/**
	 * 设置{@linkplain ResultJsonRule#setDataJsonPath(String)}。
	 * 
	 * @param jsonRule
	 *            允许{@code null}
	 * @param rstDataJsonPath
	 * @return 非{@code null}，已设置的实例
	 */
	public static ResultJsonRule setResultDataJsonPath(ResultJsonRule jsonRule, String rstDataJsonPath)
	{
		if (jsonRule == null)
			jsonRule = new ResultJsonRule();

		jsonRule.setDataJsonPath(rstDataJsonPath);

		return jsonRule;
	}

	/**
	 * 获取{@linkplain ResultJsonRule#getAdditionJsonPath()}。
	 * 
	 * @param jsonRule
	 *            允许{@code null}
	 * @return
	 */
	public static String getResultAdditionJsonPath(ResultJsonRule jsonRule)
	{
		return (jsonRule == null ? null : jsonRule.getAdditionJsonPath());
	}

	/**
	 * 设置{@linkplain ResultJsonRule#setAdditionJsonPath(String)}。
	 * 
	 * @param jsonRule
	 *            允许{@code null}
	 * @param additionJsonPath
	 * @return 非{@code null}，已设置的实例
	 */
	public static ResultJsonRule setResultAdditionJsonPath(ResultJsonRule jsonRule, String additionJsonPath)
	{
		if (jsonRule == null)
			jsonRule = new ResultJsonRule();

		jsonRule.setAdditionJsonPath(additionJsonPath);

		return jsonRule;
	}
}
