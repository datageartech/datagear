/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数值解析器。
 * 
 * @author datagear@163.com
 *
 */
public class NumberParser
{
	public NumberParser()
	{
		super();
	}

	/**
	 * 解析{@code Byte}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Byte parseByte(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.byteValue());
	}

	/**
	 * 尽量解析为{@code Short}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseByteIfExact(String value) throws NumberParserException
	{
		Number number = parseShortIfExact(value);

		if (number == null)
			return null;

		if (number instanceof Short)
		{
			Short v = (Short) number;

			if (v.byteValue() == v.shortValue())
				return v.byteValue();
			else
				return v;
		}
		else
			return number;
	}

	/**
	 * 解析{@code Integer}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Short parseShort(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.shortValue());
	}

	/**
	 * 尽量解析为解析{@code Short}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseShortIfExact(String value) throws NumberParserException
	{
		Number number = parseIntIfExact(value);

		if (number == null)
			return null;

		if (number instanceof Integer)
		{
			Integer v = (Integer) number;

			if (v.shortValue() == v.intValue())
				return v.shortValue();
			else
				return v;
		}
		else
			return number;
	}

	/**
	 * 解析{@code Integer}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Integer parseInt(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.intValue());
	}

	/**
	 * 尽量解析为解析{@code Integer}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseIntIfExact(String value) throws NumberParserException
	{
		Number number = parseLongIfExact(value);

		if (number == null)
			return null;

		if (number instanceof Long)
		{
			Long v = (Long) number;

			if (v.intValue() == v.longValue())
				return v.intValue();
			else
				return v;
		}
		else
			return number;
	}

	/**
	 * 解析{@code Long}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Long parseLong(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.longValue());
	}

	/**
	 * 尽量解析为解析{@code Long}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseLongIfExact(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);

		if (number == null)
			return null;

		try
		{
			return number.longValueExact();
		}
		catch (ArithmeticException e)
		{
			return number;
		}
	}

	/**
	 * 解析{@linkplain BigInteger}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public BigInteger parseBigInteger(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.toBigInteger());
	}

	/**
	 * 解析{@code Float}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Float parseFloat(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.floatValue());
	}

	/**
	 * 解析{@code Float}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseFloatIfExact(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);

		if (number == null)
			return null;

		float v = number.floatValue();

		if (v == Float.NEGATIVE_INFINITY || v == Float.POSITIVE_INFINITY)
			return parseDoubleIfExact(value);
		else
			return v;
	}

	/**
	 * 解析{@code Double}。
	 * <p>
	 * 注意：此方法会截断超出范围的原始数值。
	 * </p>
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Double parseDouble(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);
		return (number == null ? null : number.doubleValue());
	}

	/**
	 * 解析{@code Double}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public Number parseDoubleIfExact(String value) throws NumberParserException
	{
		BigDecimal number = doParseBigDecimalNullable(value);

		if (number == null)
			return null;

		double v = number.doubleValue();

		if (v == Double.NEGATIVE_INFINITY || v == Double.POSITIVE_INFINITY)
			return number;
		else
			return v;
	}

	/**
	 * 解析{@linkplain BigDecimal}。
	 * 
	 * @param value
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	public BigDecimal parseBigDecimal(String value) throws NumberParserException
	{
		return doParseBigDecimalNullable(value);
	}

	/**
	 * 将字符串解析为{@linkplain BigDecimal}。
	 * 
	 * @param value
	 *            可能为为{@code null}、{@code ""}
	 * @return 当{@code value}为{@code null}、{@code ""}时返回{@code null}
	 * @throws NumberParserException
	 */
	protected BigDecimal doParseBigDecimalNullable(String value) throws NumberParserException
	{
		if (value == null || value.isEmpty())
			return null;

		return doParseBigDecimal(value);
	}

	/**
	 * 将字符串解析为{@linkplain BigDecimal}。
	 * 
	 * @param value
	 *            不会为{@code null}、{@code ""}
	 * @return
	 * @throws NumberParserException
	 */
	protected BigDecimal doParseBigDecimal(String value) throws NumberParserException
	{
		return new BigDecimal(value);
	}
}
