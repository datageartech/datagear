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

import org.datagear.analysis.support.ResultJsonRuleAware;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain ResultJsonRuleAware}拆分存储相关实体。
 * 
 * @author datagear@163.com
 *
 */
public interface ResultJsonRuleAwareSplitEntity extends ResultJsonRuleAwareEntity
{
	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * <p>
	 * 如果子类不需要，应该返回{@code null}，不要抛出异常。
	 * </p>
	 * 
	 * @return
	 */
	@JsonIgnore
	String getRstDataJsonPath();

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * <p>
	 * 如果子类不需要，应该留空，不要抛出异常。
	 * </p>
	 * 
	 * @param rstDataJsonPath
	 */
	@JsonIgnore
	void setRstDataJsonPath(String rstDataJsonPath);

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * <p>
	 * 如果子类不需要，应该返回{@code null}，不要抛出异常。
	 * </p>
	 * 
	 * @return
	 */
	@JsonIgnore
	String getRstAdditionJsonPath();

	/**
	 * 仅用于ORM，避免过长映射名导致数据库兼容错误
	 * <p>
	 * 如果子类不需要，应该留空，不要抛出异常。
	 * </p>
	 * 
	 * @param rstAdditionJsonPath
	 */
	@JsonIgnore
	void setRstAdditionJsonPath(String rstAdditionJsonPath);
}
