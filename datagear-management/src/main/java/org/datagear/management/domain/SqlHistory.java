/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.util.Date;

import org.datagear.model.support.AbstractStringIdEntity;

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
