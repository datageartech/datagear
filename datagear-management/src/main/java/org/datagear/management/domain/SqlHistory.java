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

package org.datagear.management.domain;

import java.util.Date;

/**
 * SQL历史。
 * 
 * @author datagear@163.com
 *
 */
public class SqlHistory extends AbstractStringIdEntity
{
	private static final long serialVersionUID = 1L;

	/** SQL语句 */
	private String sql;

	/** 数据源ID */
	private String schemaId;

	/** 用户ID */
	private String userId;

	/** SQL时间 */
	private Date createTime;

	public SqlHistory()
	{
		super();
	}

	public SqlHistory(String id, String sql, String schemaId, String userId)
	{
		super(id);
		this.sql = sql;
		this.schemaId = schemaId;
		this.userId = userId;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	public String getSchemaId()
	{
		return schemaId;
	}

	public void setSchemaId(String schemaId)
	{
		this.schemaId = schemaId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
}
