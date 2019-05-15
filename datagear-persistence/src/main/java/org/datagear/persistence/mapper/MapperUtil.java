/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

/**
 * 映射工具类。
 * 
 * @author datagear@163.com
 *
 */
public class MapperUtil
{
	private MapperUtil()
	{
	}

	/**
	 * 是否是{@linkplain ModelTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isModelTableMapper(Mapper mapper)
	{
		return (mapper instanceof ModelTableMapper);
	}

	/**
	 * 强转为{@linkplain ModelTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static ModelTableMapper castModelTableMapper(Mapper mapper)
	{
		return (ModelTableMapper) mapper;
	}

	/**
	 * 是否是{@linkplain PropertyTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isPropertyTableMapper(Mapper mapper)
	{
		return (mapper instanceof PropertyTableMapper);
	}

	/**
	 * 强转为{@linkplain PropertyTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static PropertyTableMapper castPropertyTableMapper(Mapper mapper)
	{
		return (PropertyTableMapper) mapper;
	}

	/**
	 * 是否是{@linkplain JoinTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isJoinTableMapper(Mapper mapper)
	{
		return (mapper instanceof JoinTableMapper);
	}

	/**
	 * 强转为{@linkplain JoinTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static JoinTableMapper castJoinTableMapper(Mapper mapper)
	{
		return (JoinTableMapper) mapper;
	}
}
