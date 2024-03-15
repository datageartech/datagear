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

import java.util.ArrayList;
import java.util.List;

import org.datagear.util.StringUtil;

/**
 * {@linkplain NameAware}工具类。
 * 
 * @author datagear@163.com
 *
 */
public class NameAwareUtil
{
	/**
	 * 获取指定名称的{@linkplain NameAware}对象，没找到则返回{@code null}。
	 * 
	 * @param <T>
	 * @param list
	 *            允许为{@code null}
	 * @param name
	 * @return
	 */
	public static <T extends NameAware> T find(List<T> list, String name)
	{
		int index = findIndex(list, name);
		return (index < 0 ? null : list.get(index));
	}

	/**
	 * 获取指定名称的{@linkplain NameAware}的索引。
	 * 
	 * @param <T>
	 * @param list
	 *            允许为{@code null}
	 * @param name
	 * @return 返回{@code -1}表示未找到
	 */
	public static <T extends NameAware> int findIndex(List<T> list, String name)
	{
		if(list == null)
			return  -1;
		
		for (int i = 0, len = list.size(); i < len; i++)
		{
			if (StringUtil.isEquals(name, list.get(i).getName()))
				return i;
		}

		return -1;
	}

	/**
	 * 查找与名称列表对应的{@linkplain NameAware}列表。
	 * <p>
	 * 如果{@code names}某元素没有对应的{@linkplain NameAware}，返回列表对应元素位置将为{@code null}。
	 * </p>
	 * 
	 * @param <T>
	 * @param list 允许为{@code null}
	 * @param names
	 * @return
	 */
	public static <T extends NameAware> List<T> finds(List<T> list, List<String> names)
	{
		List<T> re = new ArrayList<>(names.size());
		
		for (String name : names)
			re.add(find(list, name));
		
		return re;
	}
}
