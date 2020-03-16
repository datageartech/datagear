/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 不支持的JDBC数据类型异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedJdbcTypeException extends DatabaseModelResolverException
{
	private static final long serialVersionUID = 1L;

	private int jdbcType;

	public UnsupportedJdbcTypeException()
	{
		super();
	}

	public UnsupportedJdbcTypeException(int jdbcType, String message)
	{
		super(message);
		this.jdbcType = jdbcType;
	}

	public UnsupportedJdbcTypeException(int jdbcType, Throwable cause)
	{
		super(cause);
		this.jdbcType = jdbcType;
	}

	public UnsupportedJdbcTypeException(int jdbcType, String message, Throwable cause)
	{
		super(message, cause);
		this.jdbcType = jdbcType;
	}

	public int getJdbcType()
	{
		return jdbcType;
	}

	public void setJdbcType(int jdbcType)
	{
		this.jdbcType = jdbcType;
	}
}
