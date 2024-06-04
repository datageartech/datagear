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
public class SqlHistory extends AbstractStringIdEntity implements CreateTimeEntity
{
	private static final long serialVersionUID = 1L;

	/** SQL语句 */
	private String sql;

	/** 数据源ID */
	private String dtbsSourceId;

	/** 用户ID */
	private String userId;

	/** SQL时间 */
	private Date createTime = null;

	public SqlHistory()
	{
		super();
	}

	public SqlHistory(String id, String sql, String dtbsSourceId, String userId)
	{
		super(id);
		this.sql = sql;
		this.dtbsSourceId = dtbsSourceId;
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

	public String getDtbsSourceId()
	{
		return dtbsSourceId;
	}

	public void setDtbsSourceId(String dtbsSourceId)
	{
		this.dtbsSourceId = dtbsSourceId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	@Override
	public Date getCreateTime()
	{
		return createTime;
	}

	@Override
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
}
