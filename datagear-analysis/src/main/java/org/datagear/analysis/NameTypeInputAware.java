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

package org.datagear.analysis;

/**
 * 名称、类型、界面输入相关类接口。
 * 
 * @author datagear@163.com
 *
 */
public interface NameTypeInputAware extends NameTypeAware
{
	/**
	 * 是否必填。
	 * 
	 * @return
	 */
	boolean isRequired();
	
	/**
	 * 获取输入框类型。
	 * 
	 * @return 可能为{@code null}
	 */
	String getInputType();

	/**
	 * 获取输入框载荷。
	 * <p>
	 * 比如：下拉选择输入框时，可以用于定义选项数组；日期输入框时，可以用于定义日期格式字符串。
	 * </p>
	 * 
	 * @return 可能为{@code null}
	 */
	Object getInputPayload();
}
