/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC类型映射支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcTypeMapSupport
{
	public JdbcTypeMapSupport()
	{
		super();
	}

	/**
	 * 将JDBC类型字面映射表转换为JDBC类型值映射表。
	 * 
	 * @param literalMap
	 * @return
	 */
	protected <T> Map<Integer, T> literalMapToValueMap(Map<String, T> literalMap)
	{
		Map<Integer, T> valueMap = new HashMap<Integer, T>();

		for (Map.Entry<String, T> entry : literalMap.entrySet())
		{
			try
			{
				java.lang.reflect.Field field = Types.class.getField(entry.getKey());

				Integer jdbcType = (Integer) field.get(null);

				valueMap.put(jdbcType, entry.getValue());
			}
			catch (NoSuchFieldException e)
			{
				throw new IllegalArgumentException("Getting field [" + entry.getKey() + "] value of ["
						+ Types.class.getName() + "] exception occurs, maybe no such field");
			}
			catch (IllegalAccessException e)
			{
				throw new IllegalArgumentException("Getting field [" + entry.getKey() + "] value of ["
						+ Types.class.getName() + "] exception occurs, maybe no such field");
			}
		}

		return valueMap;
	}
}
