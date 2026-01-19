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

package org.datagear.analysis.form;

import java.util.List;

/**
 * 分组表单属性集。
 * 
 * @author datagear@163.com
 *
 */
public interface GroupFormProperties
{
	String PROPERTY_PROPERTIES = "properties";
	String PROPERTY_GROUPS = "groups";

	/**
	 * {@linkplain FormProperty}列表。
	 * 
	 * @return {@code null}或空表示没有
	 */
	List<FormProperty> getProperties();

	/**
	 * {@linkplain FormPropertyGroup}列表。
	 * <p>
	 * 其中的{@linkplain FormPropertyGroup#getNames()}是上述{@linkplain #getProperties()}中的{@linkplain FormProperty#getName()}。
	 * </p>
	 * 
	 * @return {@code null}或空表示没有
	 */
	List<FormPropertyGroup> getGroups();
}
