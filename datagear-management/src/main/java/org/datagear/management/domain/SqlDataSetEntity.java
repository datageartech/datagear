/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Date;
import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.util.resource.ConnectionFactory;

/**
 * {@linkplain SqlDataSetEntity}实体。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetEntity extends SqlDataSet implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "DataSet";

	/** 名称 */
	private String name;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public SqlDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public SqlDataSetEntity(String id, List<DataSetProperty> properties, SchemaConnectionFactory connectionFactory,
			String sql, String name, User createUser)
	{
		super(id, properties, connectionFactory, sql);
		this.name = name;
		this.createTime = new Date();
	}

	@Override
	public SchemaConnectionFactory getConnectionFactory()
	{
		return (SchemaConnectionFactory) super.getConnectionFactory();
	}

	@Override
	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		if (connectionFactory != null && !(connectionFactory instanceof SchemaConnectionFactory))
			throw new IllegalArgumentException();

		super.setConnectionFactory(connectionFactory);
	}

	public SchemaConnectionFactory getSchemaConnectionFactory()
	{
		return getConnectionFactory();
	}

	public void setSchemaConnectionFactory(SchemaConnectionFactory schemaConnectionFactory)
	{
		setConnectionFactory(schemaConnectionFactory);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public void setCreateUser(User createUser)
	{
		this.createUser = createUser;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}
}
