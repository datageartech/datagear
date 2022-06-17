/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

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
	 * 查找关键字相似的元素。
	 * 
	 * @param <T>
	 * @param map
	 * @param key
	 * @return {@code null}表示没有
	 */
	protected <T> T findLikeKey(Map<String, ? extends T> map, String key)
	{
		key = key.toUpperCase();

		for (Map.Entry<String, ? extends T> entry : map.entrySet())
		{
			String myKey = entry.getKey();

			if (key.indexOf(myKey.toUpperCase()) > -1)
				return entry.getValue();
		}

		return null;
	}
}
