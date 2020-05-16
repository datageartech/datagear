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

	public static final String PROPERTY_LABELS_SPLITTER = ",";

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private transient String _propertyLabelsText;

	public SqlDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public SqlDataSetEntity(String id, String name, List<DataSetProperty> properties,
			SchemaConnectionFactory connectionFactory,
			String sql, User createUser)
	{
		super(id, name, properties, connectionFactory, sql);
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

	@Override
	public void setProperties(List<DataSetProperty> properties)
	{
		super.setProperties(properties);
		
		if(this._propertyLabelsText != null)
			setPropertyLabelsText(properties, this._propertyLabelsText);
	}

	public void setPropertyLabelsText(String text)
	{
		List<DataSetProperty> properties = getProperties();
		
		if(properties == null || properties.isEmpty())
			this._propertyLabelsText = text;
		else
		{
			this._propertyLabelsText = null;
			setPropertyLabelsText(properties, text);
		}
	}

	public String getPropertyLabelsText()
	{
		return DataSetProperty.concatLabels(getProperties(), PROPERTY_LABELS_SPLITTER);
	}

	protected void setPropertyLabelsText(List<DataSetProperty> properties, String text)
	{
		String[] labels = DataSetProperty.splitLabels(text, PROPERTY_LABELS_SPLITTER);

		if (labels == null || labels.length == 0)
			return;

		for (int i = 0; i < Math.min(labels.length, properties.size()); i++)
		{
			if (i < labels.length)
				properties.get(i).setLabel(labels[i]);
		}
	}
}
