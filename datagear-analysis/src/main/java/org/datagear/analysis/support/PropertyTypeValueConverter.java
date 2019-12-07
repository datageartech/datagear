package org.datagear.analysis.support;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.datagear.analysis.PropertyType;

/**
 * {@linkplain PropertyType}类型值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyTypeValueConverter
{
	public PropertyTypeValueConverter()
	{
		super();
	}

	/**
	 * 将对象转换为指定{@linkplain PropertyType}所描述类型的值。
	 * 
	 * @param propertyType
	 * @param value
	 * @return
	 */
	public Object convert(PropertyType propertyType, Object value)
	{
		if (PropertyType.STRING.equals(propertyType))
		{
			return convertToString(value);
		}
		else if (PropertyType.BOOLEAN.equals(propertyType))
		{
			return convertToBoolean(value);
		}
		else if (PropertyType.NUMBER.equals(propertyType))
		{
			return convertToNumber(value);
		}
		else
			throw new UnsupportedOperationException();
	}

	protected Object convertToString(Object value)
	{
		if (value == null)
			return null;
		else if (value instanceof String)
			return value;
		else
			return value.toString();
	}

	protected Object convertToBoolean(Object value)
	{
		if (value == null)
			return Boolean.FALSE;
		else if (value instanceof Boolean)
			return value;
		else if (value instanceof String)
		{
			String str = (String) value;

			return ("true".equalsIgnoreCase(str) || "1".equals(str) || "on".equalsIgnoreCase(str)
					|| "yes".equalsIgnoreCase(str));
		}
		else if (value instanceof Number)
		{
			return (((Number) value).intValue() > 0);
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + value.getClass().getName()
					+ "] to [" + PropertyType.BOOLEAN + "] is not supported");
	}

	protected Number convertToNumber(Object value)
	{
		if (value == null)
			return null;
		else if (value instanceof Number)
		{
			return (Number) value;
		}
		else if (value instanceof String)
		{
			String str = (String) value;

			if (str.indexOf('.') > -1)
				return new BigDecimal((String) value);
			else
				return new BigInteger(str);
		}
		else
			throw new UnsupportedOperationException("Convert object of type [" + value.getClass().getName()
					+ "] to [" + PropertyType.NUMBER + "] is not supported");
	}
}
