/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数据集属性信息。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(DataSetQuery)}返回的{@linkplain DataSetResult#getData()}元素的属性信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetProperty extends AbstractNameTypeAware implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	/** 展示标签 */
	private String label = null;

	/** 默认值 */
	private Object defaultValue = null;

	public DataSetProperty()
	{
		super();
	}

	public DataSetProperty(String name, String type)
	{
		super(name, type);
	}

	public DataSetProperty(DataSetProperty property)
	{
		super(property.getName(), property.getType());
		this.label = property.label;
		this.defaultValue = property.defaultValue;
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

	/**
	 * 获取默认值，可能为{@code null}。
	 * <p>
	 * 如果数据中此属性值为{@code null}，那么应该设置为此默认值。
	 * </p>
	 * <p>
	 * 注意：此默认值类型不一定是期望的数据类型，应当先对其进行类型转换。
	 * </p>
	 * 
	 * @return
	 */
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public DataSetProperty clone()
	{
		return new DataSetProperty(this);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", label=" + label
				+ ", defaultValue=" + defaultValue + "]";
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
