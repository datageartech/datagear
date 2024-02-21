/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.meta.resolver;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 数据库表类型规范。
 * 
 * @author datagear@163.com
 *
 */
public class DbTableTypeSpec implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 数据库匹配模式 */
	private String dbPattern;

	/** 表类型 */
	private List<String> tables = Collections.emptyList();

	/** 数据表类型 */
	private List<String> datas = Collections.emptyList();

	/** 实体表类型 */
	private List<String> entites = Collections.emptyList();

	public DbTableTypeSpec()
	{
		super();
	}

	public DbTableTypeSpec(String dbPattern)
	{
		super();
		this.dbPattern = dbPattern;
	}

	public DbTableTypeSpec(String dbPattern, List<String> tables)
	{
		super();
		this.dbPattern = dbPattern;
		this.tables = tables;
	}

	public DbTableTypeSpec(String dbPattern, List<String> tables, List<String> datas)
	{
		super();
		this.dbPattern = dbPattern;
		this.tables = tables;
		this.datas = datas;
	}

	public DbTableTypeSpec(String dbPattern, List<String> tables, List<String> datas, List<String> entites)
	{
		super();
		this.dbPattern = dbPattern;
		this.tables = tables;
		this.datas = datas;
		this.entites = entites;
	}

	public String getDbPattern()
	{
		return dbPattern;
	}

	public void setDbPattern(String dbPattern)
	{
		this.dbPattern = dbPattern;
	}

	/**
	 * 获取所有表类型。
	 * 
	 * @return 空列表表示未定义
	 */
	public List<String> getTables()
	{
		return tables;
	}

	public void setTables(List<String> tables)
	{
		this.tables = tables;
	}

	/**
	 * 获取数据表类型。
	 * 
	 * @return 空列表表示未定义
	 */
	public List<String> getDatas()
	{
		return datas;
	}

	public void setDatas(List<String> datas)
	{
		this.datas = datas;
	}

	/**
	 * 获取实体表类型模式。
	 * 
	 * @return 空列表表示未定义
	 */
	public List<String> getEntities()
	{
		return entites;
	}

	public void setEntities(List<String> entites)
	{
		this.entites = entites;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dbPattern=" + dbPattern + ", tables=" + tables + ", datas=" + datas
				+ ", entites=" + entites + "]";
	}
}
