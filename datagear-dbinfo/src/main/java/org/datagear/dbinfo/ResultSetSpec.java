/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * 抽象{@linkplain ResultSet}规范。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class ResultSetSpec<T extends ResultSetSpecBean>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultSetSpec.class);

	public ResultSetSpec()
	{
		super();
	}

	/**
	 * 读取列表。
	 * 
	 * @param rs
	 * @return
	 * @throws ResultSetIncompatibleException
	 * @throws SQLException
	 */
	public List<T> read(ResultSet rs) throws ResultSetIncompatibleException, SQLException
	{
		return read(rs, 0, -1);
	}

	/**
	 * 读取列表。
	 * 
	 * @param rs
	 * @param startRow
	 *            起始行号，以{@code 1}开始计数
	 * @param count
	 *            读取记录数，如果{@code <0}，表示读取全部
	 * @return
	 * @throws ResultSetIncompatibleException
	 * @throws SQLException
	 */
	public List<T> read(ResultSet rs, long startRow, int count) throws ResultSetIncompatibleException, SQLException
	{
		Class<T> rowType = getRowType();
		RsColumnSpec<?, ?>[] rsColumnSpecs = getRsColumnSpecs();

		return doRead(rs, rsColumnSpecs, rowType, startRow, count);
	}

	/**
	 * 获取行目标类型。
	 * 
	 * @return
	 */
	protected abstract Class<T> getRowType();

	/**
	 * 获取{@linkplain RsColumnSpec}数组。
	 * 
	 * @return
	 */
	protected abstract RsColumnSpec<?, ?>[] getRsColumnSpecs();

	/**
	 * 读取列表。
	 * 
	 * @param rs
	 * @param rsColumnSpecs
	 * @param type
	 * @param startRow
	 *            起始行号，以{@code 1}开始计数
	 * @param count
	 *            读取记录数，如果{@code <0}，表示读取全部
	 * @return
	 * @throws ResultSetIncompatibleException
	 * @throws SQLException
	 */
	protected List<T> doRead(ResultSet rs, RsColumnSpec<?, ?>[] rsColumnSpecs, Class<T> type, long startRow, int count)
			throws ResultSetIncompatibleException, SQLException
	{
		if (startRow < 1)
			startRow = 1;

		List<T> list = new ArrayList<T>();

		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();

		boolean[] presents = new boolean[rsColumnSpecs.length];
		int[] columnIndexes = new int[rsColumnSpecs.length];

		for (int i = 1; i <= columnCount; i++)
		{
			for (int j = 0; j < rsColumnSpecs.length; j++)
			{
				RsColumnSpec<?, ?> rsColumnSpec = rsColumnSpecs[j];

				if (rsm.getColumnLabel(i).equalsIgnoreCase(rsColumnSpec.getName()))
				{
					presents[j] = true;
					columnIndexes[j] = i;
				}
			}
		}

		long row = 1;
		int readCount = 0;
		while (rs.next())
		{
			if (count >= 0 && readCount >= count)
				break;

			if (row >= startRow)
			{
				T bean = createInstance(type);
				BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);

				try
				{
					for (int i = 0; i < rsColumnSpecs.length; i++)
					{
						@SuppressWarnings("unchecked")
						RsColumnSpec<Object, Object> rsColumnSpec = (RsColumnSpec<Object, Object>) rsColumnSpecs[i];

						Object propValue = getPropertyValue(rs, row, rsColumnSpec, presents[i], columnIndexes[i]);

						beanWrapper.setPropertyValue(rsColumnSpec.getPropertyName(), propValue);
					}

					addToList(list, bean);
				}
				// 如果违反规范，则忽略此行数据，避免整个功能无法使用
				catch (ResultSetValueNullException e)
				{
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("Reading [" + type.getName() + "] ignores a row data [" + bean + "] ", e);
				}

				readCount++;
			}

			row++;
		}

		return list;
	}

	/**
	 * 添加元素。
	 * <p>
	 * 注意：子类应该重写此方法并避免添加重复元素。
	 * </p>
	 * 
	 * @param list
	 * @param bean
	 */
	protected void addToList(List<T> list, T bean)
	{
		if (list.contains(bean))
			return;

		list.add(bean);
	}

	protected boolean equalsWithNull(Object s0, Object s1)
	{
		if (s0 == null)
			return (s1 == null);
		else
			return s0.equals(s1);
	}

	/**
	 * 创建实例。
	 * 
	 * @param clazz
	 * @return
	 */
	protected <TT> TT createInstance(Class<TT> clazz)
	{
		try
		{
			return clazz.newInstance();
		}
		catch (Exception e)
		{
			throw new ResultSetIncompatibleException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <PT> PT getPropertyValue(ResultSet rs, long row, RsColumnSpec<?, PT> rsColumnSpec, boolean columnPresents,
			int columnIndex) throws ResultSetValueNullException, SQLException
	{
		Object columnValue = null;

		if (columnPresents)
		{
			columnValue = getColumnValue(rs, row, columnIndex, rsColumnSpec.getType());

			if (columnValue == null && !rsColumnSpec.isNullable())
				throw new ResultSetValueNullException(rsColumnSpec.getName());
		}

		if (columnValue == null)
			columnValue = rsColumnSpec.getDefaultValue();

		if (rsColumnSpec.hasConverter())
		{
			Converter<Object, PT> converter = (Converter<Object, PT>) rsColumnSpec.getConverter();

			return converter.convert(columnValue);
		}
		else
			return (PT) columnValue;
	}

	/**
	 * 从{@linkplain ResultSet}中读取{@linkplain String}。
	 * 
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws DatabaseInfoResolverException
	 */
	protected String getString(ResultSet rs, String columnName) throws DatabaseInfoResolverException
	{
		try
		{
			return rs.getString(columnName);
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	/**
	 * 从{@linkplain ResultSet}中读取{@code int}。
	 * 
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws DatabaseInfoResolverException
	 */
	protected int getInt(ResultSet rs, String columnName) throws DatabaseInfoResolverException
	{
		try
		{
			return rs.getInt(columnName);
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	/**
	 * 获取指定类型的列值。
	 * 
	 * @param rs
	 * @param row
	 * @param columnIndex
	 * @param targetType
	 * @return
	 * @throws DatabaseInfoResolverException
	 */
	protected Object getColumnValue(ResultSet rs, long row, int columnIndex, Class<?> targetType)
			throws DatabaseInfoResolverException
	{
		try
		{
			return JdbcUtil.getColumnValue(rs, row, columnIndex, targetType);
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	/**
	 * 结果集列规范。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <CT>
	 * @param <PT>
	 */
	public static class RsColumnSpec<CT, PT> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 列名称 */
		private String name;

		/** 列类型 */
		private Class<CT> type;

		/** 是否必须出现 */
		private boolean required;

		/** 是否允许为null */
		private boolean nullable;

		/** 当列未出现或者列值为null时的默认列值 */
		private CT defaultValue;

		/** 对应的属性名称 */
		private String propertyName;

		/** 列值到属性值的转换器 */
		private Converter<CT, PT> converter;

		public RsColumnSpec()
		{
			super();
		}

		public RsColumnSpec(String name, Class<CT> type, boolean required, boolean nullable, String propertyName)
		{
			super();
			this.name = name;
			this.type = type;
			this.required = required;
			this.nullable = nullable;
			this.propertyName = propertyName;
		}

		public RsColumnSpec(String name, Class<CT> type, boolean required, boolean nullable, String propertyName,
				Converter<CT, PT> converter)
		{
			super();
			this.name = name;
			this.type = type;
			this.required = required;
			this.nullable = nullable;
			this.propertyName = propertyName;
			this.converter = converter;
		}

		public RsColumnSpec(String name, Class<CT> type, boolean required, boolean nullable, CT defaultValue,
				String propertyName)
		{
			super();
			this.name = name;
			this.type = type;
			this.required = required;
			this.nullable = nullable;
			this.propertyName = propertyName;
			this.defaultValue = defaultValue;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public Class<CT> getType()
		{
			return type;
		}

		public void setType(Class<CT> type)
		{
			this.type = type;
		}

		public boolean isRequired()
		{
			return required;
		}

		public void setRequired(boolean required)
		{
			this.required = required;
		}

		public boolean isNullable()
		{
			return nullable;
		}

		public void setNullable(boolean nullable)
		{
			this.nullable = nullable;
		}

		public CT getDefaultValue()
		{
			return defaultValue;
		}

		public void setDefaultValue(CT defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		public String getPropertyName()
		{
			return propertyName;
		}

		public void setPropertyName(String propertyName)
		{
			this.propertyName = propertyName;
		}

		public boolean hasConverter()
		{
			return (this.converter != null);
		}

		public Converter<CT, PT> getConverter()
		{
			return converter;
		}

		public void setConverter(Converter<CT, PT> converter)
		{
			this.converter = converter;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", required=" + required
					+ ", nullable=" + nullable + ", defaultValue=" + defaultValue + ", propertyName=" + propertyName
					+ ", converter=" + converter + "]";
		}

	}

	/**
	 * 类型转换器。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <S>
	 * @param <T>
	 */
	public static interface Converter<S, T>
	{
		/**
		 * 将源对象转换为目标对象。
		 * 
		 * @param s
		 * @return
		 * @throws ResultSetIncompatibleException
		 */
		T convert(S s) throws ResultSetIncompatibleException;
	}
}
