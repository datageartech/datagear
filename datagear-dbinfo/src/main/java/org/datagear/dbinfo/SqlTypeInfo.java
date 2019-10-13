/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.dbinfo;

import java.io.Serializable;
import java.sql.DatabaseMetaData;

/**
 * SQL类型信息。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTypeInfo extends ResultSetSpecBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;

	/** SQL类型，对应java.sql.Types中的值 */
	private int type;

	/** 可搜索类型 */
	private SearchableType searchableType;

	public SqlTypeInfo()
	{
		super();
	}

	public SqlTypeInfo(String name, int type, SearchableType searchableType)
	{
		super();
		this.name = name;
		this.type = type;
		this.searchableType = searchableType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public SearchableType getSearchableType()
	{
		return searchableType;
	}

	public void setSearchableType(SearchableType searchableType)
	{
		this.searchableType = searchableType;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", searchableType=" + searchableType
				+ "]";
	}

	/**
	 * 查找指定SQL类型的{@linkplain SqlTypeInfo}。
	 * <p>
	 * 如果未找到，返回{@code null}。
	 * </p>
	 * 
	 * @param sqlTypeInfos
	 * @param type
	 * @return
	 */
	public static SqlTypeInfo findByType(SqlTypeInfo[] sqlTypeInfos, int type)
	{
		if (sqlTypeInfos == null)
			return null;

		for (SqlTypeInfo sqlTypeInfo : sqlTypeInfos)
		{
			if (sqlTypeInfo.getType() == type)
				return sqlTypeInfo;
		}

		return null;
	}

	/**
	 * 可搜索类型（WHERE条件类型）。
	 * <p>
	 * 参考{@linkplain DatabaseMetaData#getTypeInfo()}结果集{@code SEARCHABLE}列说明。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum SearchableType
	{
		/** 不可用于WHERE */
		NO,

		/** 仅可用于WHERE中的LIKE */
		ONLY_LIKE,

		/** 仅不可用于WHERE中的LIKE */
		EXPCEPT_LIKE,

		/** 可用于WHERE中的任何情况 */
		ALL
	}
}
