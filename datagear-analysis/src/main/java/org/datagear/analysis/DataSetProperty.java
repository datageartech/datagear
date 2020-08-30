/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.datagear.util.StringUtil;

/**
 * 数据集属性信息。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(Map)}返回的{@linkplain DataSetResult#getData()}元素的属性信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetProperty extends AbstractDataNameType implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 展示标签 */
	private String label;

	public DataSetProperty()
	{
		super();
	}

	public DataSetProperty(String name, String type)
	{
		super(name, type);
	}

	public boolean hasLabel()
	{
		return (this.label != null && !this.label.isEmpty());
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", label=" + label + "]";
	}

	/**
	 * 连接给定列表的{@linkplain #getLabel()}。
	 * <p>
	 * 如果{@code dataSetProperties}为{@code null}，将返回空字符串。
	 * </p>
	 * 
	 * @param dataSetProperties
	 * @param splitter
	 */
	public static String concatLabels(List<DataSetProperty> dataSetProperties, String splitter)
	{
		if (dataSetProperties == null)
			return "";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < dataSetProperties.size(); i++)
		{
			DataSetProperty dataSetProperty = dataSetProperties.get(i);

			String label = dataSetProperty.getLabel();
			if (!StringUtil.isEmpty(label))
			{
				if (sb.length() > 0)
					sb.append(splitter);

				sb.append(label);
			}
		}

		return sb.toString();
	}

	/**
	 * 拆分由{@linkplain #concatLabels(List, String)}连接的字符串。
	 * 
	 * @param labelText
	 * @param splitter
	 * @return
	 */
	public static String[] splitLabels(String labelText, String splitter)
	{
		if (labelText == null)
			return new String[0];

		return StringUtil.split(labelText, splitter, true);
	}

	/**
	 * {@linkplain DataSetProperty#getType()}类型枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DataType
	{
		/** 字符串 */
		public static final String STRING = "STRING";

		/** 布尔值 */
		public static final String BOOLEAN = "BOOLEAN";

		/** 数值，可能是整数或者小数 */
		public static final String NUMBER = "NUMBER";

		/** 整数 */
		public static final String INTEGER = "INTEGER";

		/** 小数 */
		public static final String DECIMAL = "DECIMAL";

		/** 日期 */
		public static final String DATE = "DATE";

		/** 时间 */
		public static final String TIME = "TIME";

		/** 时间戳 */
		public static final String TIMESTAMP = "TIMESTAMP";

		/** 未知类型 */
		public static final String UNKNOWN = "UNKNOWN";

		/**
		 * 是否是{@linkplain #STRING}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isString(String dataType)
		{
			return STRING.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #BOOLEAN}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isBoolean(String dataType)
		{
			return BOOLEAN.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #NUMBER}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isNumber(String dataType)
		{
			return NUMBER.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #INTEGER}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isInteger(String dataType)
		{
			return INTEGER.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #DECIMAL}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isDecimal(String dataType)
		{
			return DECIMAL.equals(dataType);
		}

		/**
		 * 是否是数值型的。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isNumberic(String dataType)
		{
			return (isNumber(dataType) || isInteger(dataType) || isDecimal(dataType));
		}

		/**
		 * 是否是{@linkplain #DATE}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isDate(String dataType)
		{
			return DATE.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #TIME}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isTime(String dataType)
		{
			return TIME.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #TIMESTAMP}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isTimestamp(String dataType)
		{
			return TIMESTAMP.equals(dataType);
		}

		/**
		 * 是否是{@linkplain #UNKNOWN}。
		 * 
		 * @param dataType
		 * @return
		 */
		public static boolean isUnknown(String dataType)
		{
			return UNKNOWN.equals(dataType);
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

		/**
		 * 解析对象的数据类型。
		 * 
		 * @param obj
		 * @return
		 */
		public static String resolveDataType(Object obj)
		{
			if (obj instanceof String)
				return STRING;
			else if (obj instanceof Boolean)
				return BOOLEAN;
			else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long
					|| obj instanceof BigInteger)
				return INTEGER;
			else if (obj instanceof Float || obj instanceof Double || obj instanceof BigDecimal)
				return DECIMAL;
			else if (obj instanceof Number)
				return NUMBER;
			else if (obj instanceof java.sql.Time)
				return TIME;
			else if (obj instanceof java.sql.Timestamp)
				return TIMESTAMP;
			else if (obj instanceof java.sql.Date || obj instanceof java.util.Date)
				return DATE;
			else
				return UNKNOWN;
		}
	}
}
