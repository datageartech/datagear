/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Types;

/**
 * JDBC工具类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcUtil
{
	private JdbcUtil()
	{
	}

	/**
	 * 获取指定对象的JDBC类型。
	 * 
	 * @param obj
	 * @return
	 */
	public static int getJdbcType(Object obj)
	{
		if (obj == null)
			return Types.NULL;
		else
			return getJdbcType(obj.getClass());
	}

	/**
	 * 获取指定类的JDBC类型。
	 * 
	 * @param clazz
	 * @return
	 */
	public static int getJdbcType(Class<?> clazz)
	{
		if (String.class.equals(clazz))
		{
			return Types.VARCHAR;
		}
		else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz))
		{
			return Types.BOOLEAN;
		}
		else if (int.class.equals(clazz) || Integer.class.equals(clazz))
		{
			return Types.INTEGER;
		}
		else if (long.class.equals(clazz) || Long.class.equals(clazz))
		{
			return Types.BIGINT;
		}
		else if (float.class.equals(clazz) || Float.class.equals(clazz))
		{
			return Types.FLOAT;
		}
		else if (double.class.equals(clazz) || Double.class.equals(clazz))
		{
			return Types.DOUBLE;
		}
		else if (java.math.BigDecimal.class.isAssignableFrom(clazz))
		{
			return Types.NUMERIC;
		}
		else if (java.math.BigInteger.class.isAssignableFrom(clazz))
		{
			return Types.NUMERIC;
		}
		else if (java.sql.Date.class.isAssignableFrom(clazz))
		{
			return Types.DATE;
		}
		else if (java.sql.Time.class.isAssignableFrom(clazz))
		{
			return Types.TIME;
		}
		else if (java.sql.Timestamp.class.isAssignableFrom(clazz))
		{
			return Types.TIMESTAMP;
		}
		else if (java.util.Date.class.isAssignableFrom(clazz))
		{
			return Types.DATE;
		}
		else if (byte.class.equals(clazz) || Byte.class.equals(clazz))
		{
			return Types.TINYINT;
		}
		else if (short.class.equals(clazz) || Short.class.equals(clazz))
		{
			return Types.SMALLINT;
		}
		else if (char.class.equals(clazz) || Character.class.equals(clazz))
		{
			return Types.CHAR;
		}
		else if (byte[].class.equals(clazz) || Byte[].class.equals(clazz))
		{
			return Types.BINARY;
		}
		else if (java.sql.Clob.class.isAssignableFrom(clazz))
		{
			return Types.CLOB;
		}
		else if (java.sql.Blob.class.isAssignableFrom(clazz))
		{
			return Types.BLOB;
		}
		else if (java.sql.Array.class.isAssignableFrom(clazz))
		{
			return Types.ARRAY;
		}
		else if (java.sql.Struct.class.isAssignableFrom(clazz))
		{
			return Types.STRUCT;
		}
		else if (java.sql.Ref.class.isAssignableFrom(clazz))
		{
			return Types.REF;
		}
		else if (java.net.URL.class.isAssignableFrom(clazz))
		{
			return Types.REF;
		}
		else if (Class.class.equals(clazz))
		{
			return Types.JAVA_OBJECT;
		}
		else
			throw new UnsupportedOperationException("Unsupported Java type [" + clazz + "] for getting JDBC type");
	}
}
