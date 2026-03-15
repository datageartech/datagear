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
import org.datagear.analysis.support.ResultJsonRuleAware;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain ResultJsonRuleAware}实体接口。
 * 
 * @author datagear@163.com
 *
 */
public interface ResultJsonRuleAwareEntity extends ResultJsonRuleAware
{

	/**
	 * 设置{@linkplain ResultJsonRule}。
	 * 
	 * @param resultJsonRule
	 */
	void setResultJsonRule(ResultJsonRule resultJsonRule);

	/**
	 * 仅用于ORM。
	 * <p>
	 * 如果子类不需要，应该返回{@code null}，不要抛出异常。
	 * </p>
	 * 
	 * @return
	 */
	@JsonIgnore
	String getResultJsonRuleJson();

	/**
	 * 仅用于ORM
	 * <p>
	 * 如果子类不需要，应该留空，不要抛出异常。
	 * </p>
	 * 
	 * @param rstDataJsonPath
	 */
	@JsonIgnore
	void setResultJsonRuleJson(ResultJsonRule resultJsonRule);
}
