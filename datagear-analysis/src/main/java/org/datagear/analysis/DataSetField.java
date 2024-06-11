/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.analysis;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数据集字段信息。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(DataSetQuery)}返回的{@linkplain DataSetResult#getData()}元素的字段信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetField extends AbstractNameTypeAware implements Cloneable
{
	private static final long serialVersionUID = 1L;

	/** 展示标签 */
	private String label = null;

	/** 默认值 */
	private Object defaultValue = null;

	/** 是否是需计算的 */
	private boolean evaluated = false;

	/** 计算表达式 */
	private String expression = null;

	public DataSetField()
	{
		super();
	}

	public DataSetField(String name, String type)
	{
		super(name, type);
	}

	public DataSetField(DataSetField field)
	{
		super(field.getName(), field.getType());
		this.label = field.label;
		this.defaultValue = field.defaultValue;
		this.evaluated = field.evaluated;
		this.expression = field.expression;
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
	 * 如果数据中此字段值为{@code null}，那么应该设置为此默认值。
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

	/**
	 * 是否是需计算的。
	 * <p>
	 * 如果为{@code true}，应使用{@linkplain #getExpression()}表达式计算求值。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isEvaluated()
	{
		return evaluated;
	}

	public void setEvaluated(boolean evaluated)
	{
		this.evaluated = evaluated;
	}

	/**
	 * 获取表达式。
	 * 
	 * @return {@code null}或空字符串表示没有表达式
	 */
	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	@Override
	public DataSetField clone()
	{
		return new DataSetField(this);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", label=" + label
				+ ", defaultValue=" + defaultValue + ", evaluated=" + evaluated + ", expression=" + expression + "]";
	}

	/**
	 * {@linkplain DataSetField#getType()}类型枚举。
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
