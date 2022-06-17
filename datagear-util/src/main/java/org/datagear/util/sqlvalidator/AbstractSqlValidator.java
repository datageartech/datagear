/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.util.List;
import java.util.Map;

/**
 * 抽象{@linkplain SqlValidator}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSqlValidator implements SqlValidator
{
	public AbstractSqlValidator()
	{
		super();
	}

	/**
	 * 查找关键字是{@linkplain DatabaseProfile#getName()}或{@linkplain DatabaseProfile#getUrl()}子串（忽略大小写）的元素。
	 * 
	 * @param <T>
	 * @param map
	 * @param profile
	 * @param list
	 */
	protected <T> void findLikeKey(Map<String, ? extends T> map, DatabaseProfile profile, List<T> list)
	{
		String name = (profile.getName() == null ? null : profile.getName().toUpperCase());
		String url = (profile.getUrl() == null ? null : profile.getUrl().toUpperCase());

		for (Map.Entry<String, ? extends T> entry : map.entrySet())
		{
			String key = entry.getKey().toUpperCase();

			if (name != null && name.indexOf(key) > -1)
				list.add(entry.getValue());
			else if (url != null && url.indexOf(key) > -1)
				list.add(entry.getValue());
		}
	}
}
