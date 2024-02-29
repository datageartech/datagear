/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.i18n;

/**
 * 可标签对象。
 * 
 * @author datagear@163.com
 *
 */
public interface Labeled
{
	public static final String PROPERTY_NAME_LABEL = "nameLabel";
	public static final String PROPERTY_DESC_LABEL = "descLabel";

	/**
	 * 获取名称标签。
	 * 
	 * @return
	 */
	Label getNameLabel();

	/**
	 * 设置名称标签。
	 * 
	 * @param nameLabel
	 */
	void setNameLabel(Label nameLabel);

	/**
	 * 获取描述标签。
	 * 
	 * @return
	 */
	Label getDescLabel();

	/**
	 * 设置描述标签。
	 * 
	 * @param descLabel
	 */
	void setDescLabel(Label descLabel);
}
