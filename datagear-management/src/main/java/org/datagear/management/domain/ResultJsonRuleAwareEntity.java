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

package org.datagear.management.domain;

import org.datagear.analysis.support.ResultJsonRule;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain ResultJsonRule}相关实体。
 * 
 * @author datagear@163.com
 *
 */
public interface ResultJsonRuleAwareEntity
{
	/**
	 * 获取{@linkplain ResultJsonRule}。
	 * 
	 * @return
	 */
	ResultJsonRule getResultJsonRule();

	/**
	 * 设置{@linkplain ResultJsonRule}。
	 * 
	 * @param resultJsonRule
	 */
	void setResultJsonRule(ResultJsonRule resultJsonRule);

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * 
	 * @return
	 */
	@JsonIgnore
	String getRstDataJsonPath();

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * 
	 * @param rstDataJsonPath
	 */
	@JsonIgnore
	void setRstDataJsonPath(String rstDataJsonPath);

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * 
	 * @return
	 */
	@JsonIgnore
	String getRstAdditionJsonPath();

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * 
	 * @param rstAdditionJsonPath
	 */
	@JsonIgnore
	void setRstAdditionJsonPath(String rstAdditionJsonPath);
}
