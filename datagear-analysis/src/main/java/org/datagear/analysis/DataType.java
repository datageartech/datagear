/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 数据类型。
 * 
 * @author datagear@163.com
 *
 */
public enum DataType
{
	/** 字符串 */
	STRING,

	/** 布尔值 */
	BOOLEAN,

	/** 整数 */
	INTEGER,

	/** 小数 */
	DECIMAL,

	/** 日期 */
	DATE,

	/** 时间 */
	TIME,

	/** 时间戳 */
	TIMESTAMP;

	/**
	 * 是否是{@linkplain #STRING}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isString(DataType dataType)
	{
		return STRING.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #BOOLEAN}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isBoolean(DataType dataType)
	{
		return BOOLEAN.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #INTEGER}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isInteger(DataType dataType)
	{
		return INTEGER.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #DECIMAL}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isDecimal(DataType dataType)
	{
		return DECIMAL.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #INTEGER}或{@linkplain DECIMAL}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isNumber(DataType dataType)
	{
		return isInteger(dataType) || isDecimal(dataType);
	}

	/**
	 * 是否是{@linkplain #DATE}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isDate(DataType dataType)
	{
		return DATE.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #TIME}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isTime(DataType dataType)
	{
		return TIME.equals(dataType);
	}

	/**
	 * 是否是{@linkplain #TIMESTAMP}。
	 * 
	 * @param dataType
	 * @return
	 */
	public static boolean isTimestamp(DataType dataType)
	{
		return TIMESTAMP.equals(dataType);
	}

	/**
	 * 将{@linkplain #STRING}类型的值转换为{@linkplain String}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain String}类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static String castString(Object value)
	{
		return (String) value;
	}

	/**
	 * 将{@linkplain #BOOLEAN}类型的值转换为{@linkplain Boolean}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Boolean}类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Boolean castBoolean(Object value)
	{
		return (Boolean) value;
	}

	/**
	 * 将{@linkplain #INTEGER}类型的值转换为{@linkplain Number}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Number}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Number castInteger(Object value)
	{
		return (Number) value;
	}

	/**
	 * 将{@linkplain #INTEGER}类型的值转换为{@linkplain BigInteger}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Number}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static BigInteger castBigInteger(Object value)
	{
		Number number = castInteger(value);

		if (number == null)
			return null;
		else if (number instanceof BigInteger)
			return (BigInteger) number;
		else
			return BigInteger.valueOf(number.longValue());
	}

	/**
	 * 将{@linkplain #DECIMAL}类型的值转换为{@linkplain Number}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Number}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Number castDecimal(Object value)
	{
		return (Number) value;
	}

	/**
	 * 将{@linkplain #DECIMAL}类型的值转换为{@linkplain BigDecimal}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Number}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static BigDecimal castBigDecimal(Object value)
	{
		Number number = castInteger(value);

		if (number == null)
			return null;
		else if (number instanceof BigDecimal)
			return (BigDecimal) number;
		else
			return BigDecimal.valueOf(number.doubleValue());
	}

	/**
	 * 将{@linkplain #DATE}类型的值转换为{@linkplain Date}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Date}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Date castDate(Object value)
	{
		return (Date) value;
	}

	/**
	 * 将{@linkplain #TIME}类型的值转换为{@linkplain Time}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Date}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Time castTime(Object value)
	{
		Date date = castDate(value);

		if (date == null)
			return null;
		else if (date instanceof Time)
			return (Time) value;
		else
			return new Time(date.getTime());
	}

	/**
	 * 将{@linkplain #TIME}类型的值转换为{@linkplain Timestamp}值。
	 * <p>
	 * {@code value}参数必须是{@linkplain Date}或其子类型。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static Timestamp castTimestamp(Object value)
	{
		Date date = castDate(value);

		if (date == null)
			return null;
		else if (date instanceof Timestamp)
			return (Timestamp) value;
		else
			return new Timestamp(date.getTime());
	}
}
