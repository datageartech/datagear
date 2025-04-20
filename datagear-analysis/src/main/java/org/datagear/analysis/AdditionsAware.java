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

import java.util.Map;

/**
 * 带有附加属性的相关类。
 * 
 * @author datagear@163.com
 *
 */
public interface AdditionsAware
{
	public static final String PROPERTY_ADDITIONS = "additions";

	/**
	 * 获取附加属性。
	 * <p>
	 * 返回{@code null}或空表示没有。
	 * </p>
	 * 
	 * @return
	 */
	Map<String, ?> getAdditions();
}
