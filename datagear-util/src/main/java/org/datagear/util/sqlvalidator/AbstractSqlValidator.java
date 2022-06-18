/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.util.List;
import java.util.Map;

import org.datagear.util.JdbcUtil;

/**
 * 抽象{@linkplain SqlValidator}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSqlValidator implements SqlValidator
{
	protected static final String JDBC_URL_PREFIX_UPPERCASE = JdbcUtil.JDBC_URL_PREFIX.toUpperCase();

	public AbstractSqlValidator()
	{
		super();
	}

	/**
	 * 查找关键字是{@linkplain DatabaseProfile#getName()}子串、或是{@linkplain DatabaseProfile#getUrl()}开头（忽略大小写）的元素。
	 * <p>
	 * {@linkplain DatabaseProfile#hasName()}、{@linkplain DatabaseProfile#hasUrl()}为{@code false}的将不做匹配查找。
	 * </p>
	 * 
	 * @param <T>
	 * @param map
	 * @param profile
	 * @param list
	 */
	protected <T> void findMatchKey(Map<String, ? extends T> map, DatabaseProfile profile, List<T> list)
	{
		String name = (profile.hasName() ? profile.getName().toUpperCase() : null);
		String url = (profile.hasUrl() ? profile.getUrl().toUpperCase() : null);

		for (Map.Entry<String, ? extends T> entry : map.entrySet())
		{
			String key = entry.getKey().toUpperCase();

			if (name != null && name.indexOf(key) > -1)
			{
				list.add(entry.getValue());
			}
			else if (url != null && key.indexOf(JDBC_URL_PREFIX_UPPERCASE) == 0 && url.indexOf(key) == 0)
			{
				list.add(entry.getValue());
			}
		}
	}
}
