/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 表信息。
 * <p>
 * 类结构参考
 * {@linkplain DatabaseMetaData#getTables(String, String, String, String[])}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 * 
 */
public class TableInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 类型 */
	private TableType type;

	/** 注释 */
	private String comment;

	public TableInfo()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public TableType getType()
	{
		return type;
	}

	public void setType(TableType type)
	{
		this.type = type;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", comment=" + comment + "]";
	}
}
