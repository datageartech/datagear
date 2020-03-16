/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.JdbcUtil;

/**
 * SQL构建器。
 * 
 * @author datagear@163.com
 *
 */
public class SqlBuilder implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** sql语句 */
	private StringBuilder sql = new StringBuilder();

	/** 参数 */
	private List<Object> args = new ArrayList<Object>();

	/** 参数类型 */
	private List<Integer> argTypes = new ArrayList<Integer>();

	private String delimiter = null;

	private boolean isDelimiterElementAdded = false;

	public SqlBuilder()
	{
		super();
	}

	public SqlBuilder(String sql)
	{
		super();

		if (sql != null && !sql.isEmpty())
			this.sql.append(sql);
	}

	/**
	 * 获取SQL语句。
	 * 
	 * @return
	 */
	public StringBuilder getSql()
	{
		return sql;
	}

	/**
	 * 获取参数列表。
	 * 
	 * @return
	 */
	public List<Object> getArgs()
	{
		return args;
	}

	/**
	 * 获取参数类型列表。
	 * 
	 * @return
	 */
	public List<Integer> getArgTypes()
	{
		return argTypes;
	}

	/**
	 * 追加SQL语句。
	 * 
	 * @param sql
	 */
	public SqlBuilder sql(String sql)
	{
		resetDelimiterStatus();

		this.sql.append(sql);

		return this;
	}

	/**
	 * 追加SQL语句和参数。
	 * 
	 * @param sql
	 * @param arg
	 */
	public SqlBuilder sql(String sql, Object... arg)
	{
		resetDelimiterStatus();

		this.sql.append(sql);

		for (Object a : arg)
			arg(a);

		return this;
	}

	/**
	 * 追加SQL语句、参数、参数类型。
	 * 
	 * @param sql
	 * @param arg
	 * @param argType
	 * @return
	 */
	public SqlBuilder sql(String sql, Object arg, int argType)
	{
		resetDelimiterStatus();

		this.sql.append(sql);

		arg(arg, argType);

		return this;
	}

	/**
	 * 追加SQL语句、参数、参数类型。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return
	 */
	public SqlBuilder sql(String sql, Object[] args, int[] argTypes)
	{
		resetDelimiterStatus();

		this.sql.append(sql);

		if (args != null)
		{
			for (int i = 0; i < args.length; i++)
				arg(args[i], argTypes[i]);
		}

		return this;
	}

	/**
	 * 追加另一个Sql对象。
	 * 
	 * @param sql
	 */
	public SqlBuilder sql(SqlBuilder sql)
	{
		resetDelimiterStatus();

		this.sql.append(sql.getSqlString());

		this.args.addAll(sql.getArgs());
		this.argTypes.addAll(sql.getArgTypes());

		return this;
	}

	/**
	 * 定义SQL分隔符。
	 * <p>
	 * {@linkplain #sqld(String)}、{@linkplain #sqld(String, int)}、
	 * {@linkplain #sqld(String[])}、{@linkplain #sqldSuffix(String[], String)}、
	 * {@linkplain #sqldPrefix(String[], String)}
	 * 方法使用此分隔符追加SQL，连续调用这些方法不会重复添加多余的分隔符，直到再次调用此方法、或者调用任何一个 {@code sql(...)}
	 * 方法。
	 * </p>
	 * 
	 * @param delimiter
	 * @return
	 */
	public SqlBuilder delimit(String delimiter)
	{
		resetDelimiterStatus();
		this.delimiter = delimiter;

		return this;
	}

	/**
	 * 追加SQL分隔元素。
	 * 
	 * @param elements
	 * @return
	 */
	public SqlBuilder sqld(String element)
	{
		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		this.sql.append(element);

		return this;
	}

	/**
	 * 追加SQL分隔元素。
	 * 
	 * @param elements
	 * @return
	 */
	public SqlBuilder sqld(String[] elements)
	{
		if (elements == null || elements.length == 0)
			return this;

		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		for (int i = 0; i < elements.length; i++)
		{
			if (i > 0)
				this.sql.append(this.delimiter);

			this.sql.append(elements[i]);
		}

		return this;
	}

	/**
	 * 重复追加SQL分隔元素。
	 * 
	 * @param element
	 * @param count
	 *            追加次数
	 * @return
	 */
	public SqlBuilder sqld(String element, int count)
	{
		if (count <= 0)
			return this;

		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		for (int i = 0; i < count; i++)
		{
			if (i > 0)
				this.sql.append(this.delimiter);

			this.sql.append(element);
		}

		return this;
	}

	/**
	 * 追加SQL分隔元素。
	 * 
	 * @param element
	 * @return
	 */
	public SqlBuilder sqld(SqlBuilder element)
	{
		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		this.sql.append(element.sql);
		this.args.addAll(element.args);
		this.argTypes.addAll(element.argTypes);

		return this;
	}

	/**
	 * 追加SQL分隔元素。
	 * 
	 * @param elements
	 * @return
	 */
	public SqlBuilder sqld(SqlBuilder[] elements)
	{
		if (elements == null || elements.length == 0)
			return this;

		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		for (int i = 0; i < elements.length; i++)
		{
			if (i > 0)
				this.sql.append(this.delimiter);

			this.sql.append(elements[i].sql);
			this.args.addAll(elements[i].args);
			this.argTypes.addAll(elements[i].argTypes);
		}

		return this;
	}

	/**
	 * 追加SQL分隔元素，并为元素追加{@code suffix}后缀。
	 * 
	 * @param element
	 * @param suffix
	 * @return
	 */
	public SqlBuilder sqldSuffix(String element, String suffix)
	{
		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		this.sql.append(element).append(suffix);

		return this;
	}

	/**
	 * 追加SQL分隔元素，并为每一个元素追加{@code suffix}后缀。
	 * 
	 * @param elements
	 * @param suffix
	 * @return
	 */
	public SqlBuilder sqldSuffix(String[] elements, String suffix)
	{
		if (elements == null || elements.length == 0)
			return this;

		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		for (int i = 0; i < elements.length; i++)
		{
			if (i > 0)
				this.sql.append(this.delimiter);

			this.sql.append(elements[i]).append(suffix);
		}

		return this;
	}

	/**
	 * 追加SQL分隔元素，并为元素追加{@code prefix}前缀。
	 * 
	 * @param element
	 * @param prefix
	 * @return
	 */
	public SqlBuilder sqldPrefix(String element, String prefix)
	{
		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		this.sql.append(prefix).append(element);

		return this;
	}

	/**
	 * 追加SQL分隔元素，并为每一个元素追加{@code prefix}前缀。
	 * 
	 * @param elements
	 * @param prefix
	 * @return
	 */
	public SqlBuilder sqldPrefix(String[] elements, String prefix)
	{
		if (elements == null || elements.length == 0)
			return this;

		if (this.isDelimiterElementAdded)
			this.sql.append(this.delimiter);
		else
			this.isDelimiterElementAdded = true;

		for (int i = 0; i < elements.length; i++)
		{
			if (i > 0)
				this.sql.append(this.delimiter);

			this.sql.append(prefix).append(elements[i]);
		}

		return this;
	}

	/**
	 * 追加参数。
	 * 
	 * @param arg
	 */
	public SqlBuilder arg(Object arg)
	{
		this.args.add(arg);
		this.argTypes.add(getJdbcType(arg));

		return this;
	}

	/**
	 * 追加参数。
	 * 
	 * @param args
	 * @return
	 */
	public SqlBuilder arg(Object[] args)
	{
		for (Object arg : args)
		{
			this.args.add(arg);
			this.argTypes.add(getJdbcType(arg));
		}

		return this;
	}

	/**
	 * 追加参数及其类型。
	 * 
	 * @param arg
	 */
	public SqlBuilder arg(Object arg, int type)
	{
		this.args.add(arg);
		this.argTypes.add(type);

		return this;
	}

	/**
	 * 追加SQL参数类型。
	 * 
	 * @param argType
	 * @return
	 */
	public SqlBuilder argType(int argType)
	{
		this.argTypes.add(argType);

		return this;
	}

	/**
	 * 重置参数。
	 */
	public void resetArg()
	{
		this.args = new ArrayList<Object>();
		this.argTypes = new ArrayList<Integer>();
	}

	/**
	 * 此Sql是否为空。
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return (this.sql.length() == 0);
	}

	/**
	 * 获取查询语句字符串。
	 * 
	 * @return
	 */
	public String getSqlString()
	{
		return this.sql.toString();
	}

	/**
	 * 获取参数数组。
	 * 
	 * @return
	 */
	public Object[] getArgsArray()
	{
		return this.args.toArray();
	}

	/**
	 * 获取参数类型数组。
	 * 
	 * @return
	 */
	public int[] getArgTypesArray()
	{
		int[] re = new int[this.argTypes.size()];
		for (int i = 0, len = this.argTypes.size(); i < len; i++)
			re[i] = this.argTypes.get(i);

		return re;
	}

	/**
	 * 获取SQL语句的长度。
	 * 
	 * @return
	 */
	public int sqlLength()
	{
		return this.sql.length();
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数。
	 * 
	 * @param cn
	 * @param pst
	 * @throws SQLException
	 */
	public void setParamValues(Connection cn, PreparedStatement pst) throws SQLException
	{
		for (int i = 0, len = this.args.size(); i < len; i++)
			JdbcUtil.setParamValue(cn, pst, i + 1, this.argTypes.get(i), this.args.get(i));
	}

	protected void setSql(StringBuilder sql)
	{
		this.sql = sql;
	}

	protected void setArgs(List<Object> args)
	{
		this.args = args;
	}

	protected void setArgTypes(List<Integer> argTypes)
	{
		this.argTypes = argTypes;
	}

	/**
	 * 重置分隔状态。
	 */
	protected void resetDelimiterStatus()
	{
		if (this.isDelimiterElementAdded == true)
			this.isDelimiterElementAdded = false;
	}

	/**
	 * 由对象类型获取其对应的类型。
	 * 
	 * @param o
	 * @return
	 */
	protected int getJdbcType(Object o)
	{
		return JdbcUtil.getJdbcType(o);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [sql=" + sql + ", args=" + args + ", argTypes=" + argTypes + "]";
	}

	/**
	 * 创建一个新的SQL构建器。
	 * 
	 * @return
	 */
	public static SqlBuilder valueOf()
	{
		return new SqlBuilder();
	}

	/**
	 * 创建一个新的SQL构建器。
	 * 
	 * @param sql
	 * @return
	 */
	public static SqlBuilder valueOf(String sql)
	{
		return new SqlBuilder(sql);
	}

	/**
	 * 指定{@linkplain SqlBuilder}是否为{@code null}、{@linkplain #isEmpty()}。
	 * 
	 * @param sqlBuilder
	 * @return
	 */
	public static boolean isEmpty(SqlBuilder sqlBuilder)
	{
		return (sqlBuilder == null || sqlBuilder.isEmpty());
	}
}