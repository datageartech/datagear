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
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 数据集字段信息。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(DataSetQuery)}返回的{@linkplain DataSetResult#getData()}元素的字段信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetField extends AbstractNameTypeAware implements DataSetFieldsAware, FullnameAware, Cloneable
{
	private static final long serialVersionUID = 1L;

	/** 全名 */
	private String fullname;

	/** 展示标签 */
	private String label = null;

	/** 默认值 */
	private Object defaultValue = null;

	/** 是否是需计算的 */
	private boolean evaluated = false;

	/** 计算表达式 */
	private String expression = null;

	/**
	 * 对象类型的字段集合。
	 * <p>
	 * 注意：此值默认必须为{@code null}，以兼容{@code <=5.5.0}版本没有此属性的逻辑。
	 * </p>
	 */
	private List<DataSetField> fields = null;

	/**
	 * 是否数组。
	 * <p>
	 * 注意：此值默认必须为{@code false}，以兼容{@code <=5.5.0}版本没有此属性的逻辑。
	 * </p>
	 */
	private boolean array = false;

	public DataSetField()
	{
		super();
	}

	public DataSetField(String name, String type)
	{
		super(name, DataType.normalize(type));
		this.fullname = FullnameSpec.toFullname(name, null);
	}

	public DataSetField(String name, String fullname, String type)
	{
		super(name, DataType.normalize(type));
		this.fullname = fullname;
	}

	public DataSetField(DataSetField field)
	{
		super(field.getName(), DataType.normalize(field.getType()));
		this.fullname = field.fullname;
		this.label = field.label;
		this.defaultValue = field.defaultValue;
		this.evaluated = field.evaluated;
		this.expression = field.expression;
		this.fields = field.fields;
		this.array = field.array;
	}

	@Override
	public void setType(String type)
	{
		super.setType(DataType.normalize(type));
	}

	@Override
	public String getFullname()
	{
		return fullname;
	}

	public void setFullname(String fullname)
	{
		this.fullname = fullname;
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
	public List<DataSetField> getFields()
	{
		return fields;
	}

	public void setFields(List<DataSetField> fields)
	{
		this.fields = fields;
	}

	public boolean isArray()
	{
		return array;
	}

	public void setArray(boolean array)
	{
		this.array = array;
	}

	@Override
	public DataSetField getField(String name)
	{
		return NameAwareUtil.find(this.fields, name);
	}

	@Override
	public DataSetField clone()
	{
		return new DataSetField(this);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", fullname=" + fullname
				+ ", label=" + label + ", defaultValue=" + defaultValue + ", evaluated=" + evaluated + ", expression="
				+ expression + "]";
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
		public static final String STRING = "string";

		/** 布尔值 */
		public static final String BOOLEAN = "boolean";

		/** 数值，可能是整数或者小数 */
		public static final String NUMBER = "number";

		/** 整数 */
		public static final String INTEGER = "integer";

		/** 小数 */
		public static final String DECIMAL = "decimal";

		/** 日期 */
		public static final String DATE = "date";

		/** 时间 */
		public static final String TIME = "time";

		/** 时间戳 */
		public static final String TIMESTAMP = "timestamp";

		/** 对象 */
		public static final String OBJECT = "object";

		/** 未知类型 */
		public static final String UNKNOWN = "unknown";

		/**
		 * 规范类型。
		 * 
		 * @param type
		 * @return
		 */
		public static String normalize(String type)
		{
			return normalize(type, UNKNOWN);
		}

		/**
		 * 规范类型。
		 * 
		 * @param type
		 * @param dftType
		 * @return
		 */
		public static String normalize(String type, String dftType)
		{
			if (type == null)
				return dftType;

			if (STRING.equalsIgnoreCase(type))
				return STRING;

			if (BOOLEAN.equalsIgnoreCase(type))
				return BOOLEAN;

			if (NUMBER.equalsIgnoreCase(type))
				return NUMBER;

			if (INTEGER.equalsIgnoreCase(type))
				return INTEGER;

			if (DECIMAL.equalsIgnoreCase(type))
				return DECIMAL;

			if (DATE.equalsIgnoreCase(type))
				return DATE;

			if (TIME.equalsIgnoreCase(type))
				return TIME;

			if (TIMESTAMP.equalsIgnoreCase(type))
				return TIMESTAMP;

			if (OBJECT.equalsIgnoreCase(type))
				return OBJECT;

			if (UNKNOWN.equalsIgnoreCase(type))
				return UNKNOWN;

			return dftType;
		}

		/**
		 * 是否像数组的对象。
		 * 
		 * @param obj
		 * @return
		 */
		public static boolean isLikeArray(Object obj)
		{
			if (obj == null)
				return false;

			if (obj instanceof Object[])
				return true;

			if (obj instanceof Collection<?>)
				return true;

			return false;
		}

		/**
		 * 获取像数组对象的第一个元素。
		 * 
		 * @param likeArray
		 * @return
		 */
		public static Object getLikeArrayFirstEle(Object likeArray)
		{
			Object ele = null;

			if (likeArray == null)
				ele = null;
			else if (likeArray instanceof Object[])
			{
				Object[] array = (Object[]) likeArray;
				ele = (array.length > 0 ? array[0] : null);
			}
			else if (likeArray instanceof Collection<?>)
			{
				Collection<?> col = (Collection<?>) likeArray;
				for (Object o : col)
				{
					ele = o;
					break;
				}
			}

			return ele;
		}

		/**
		 * 解析像数组对象的元素数据类型。
		 * 
		 * @param likeArray
		 * @return
		 */
		public static String resolveArrayEleDataType(Object likeArray)
		{
			Object ele = getLikeArrayFirstEle(likeArray);
			return resolveDataType(ele);
		}

		/**
		 * 解析数据类型。
		 * 
		 * @param obj
		 * @return
		 */
		public static String resolveDataType(Object obj)
		{
			if (obj == null)
				return UNKNOWN;
			else if (obj instanceof String)
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
			else if (obj instanceof Map<?, ?>)
				return OBJECT;
			else
				return UNKNOWN;
		}
	}
}
