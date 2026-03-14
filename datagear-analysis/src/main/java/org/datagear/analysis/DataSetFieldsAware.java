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

import java.util.List;

/**
 * {@linkplain DataSetField}列表相关基础接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetFieldsAware
{
	/**
	 * 获取{@linkplain DataSetField}列表。
	 * 
	 * @return 返回{@code null}或空列表则表示没有
	 */
	List<DataSetField> getFields();

	/**
	 * 获取指定名称的{@linkplain DataSetField}，没有则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSetField getField(String name);
}
