/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Date;

import org.datagear.analysis.DataCategory;
import org.datagear.analysis.support.SqlDataSetFactory;
import org.datagear.util.resource.ConnectionFactory;

/**
 * {@linkplain SqlDataSetFactoryEntity}实体。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFactoryEntity extends SqlDataSetFactory
		implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	public static final String SPLITTER = ",";

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

	public SqlDataSetFactoryEntity()
	{
		super();
		this.createTime = new Date();
	}

	public SqlDataSetFactoryEntity(String id, SchemaConnectionFactory connectionFactory, String sql,
			DataCategory[] dataCategories, String name, User createUser)
	{
		super(id, connectionFactory, sql, dataCategories);
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

	public String getDataCategoriesText()
	{
		StringBuilder sb = new StringBuilder();

		DataCategory[] dataCategories = getDataCategories();

		if (dataCategories != null)
		{
			for (int i = 0; i < dataCategories.length; i++)
			{
				if (i > 0)
					sb.append(SPLITTER + " ");

				sb.append(dataCategories[i].name());
			}
		}

		return sb.toString();
	}

	public void setDataCategoriesText(String dataCategoriesText)
	{
		String[] texts = split(dataCategoriesText);

		DataCategory[] dataCategories = new DataCategory[texts.length];

		for (int i = 0; i < texts.length; i++)
		{
			String text = texts[i].trim();

			if (DataCategory.DIMENSION.name().equalsIgnoreCase(text))
				dataCategories[i] = DataCategory.DIMENSION;
			else if (DataCategory.SCALAR.name().equalsIgnoreCase(text))
				dataCategories[i] = DataCategory.SCALAR;
			else
				throw new IllegalArgumentException("illegal data categories text [" + dataCategoriesText + "]");
		}

		setDataCategories(dataCategories);
	}

	public String getColumnLabelsText()
	{
		StringBuilder sb = new StringBuilder();

		String[] columnLabels = getColumnLabels();

		if (columnLabels != null)
		{
			for (int i = 0; i < columnLabels.length; i++)
			{
				if (i > 0)
					sb.append(SPLITTER + " ");

				sb.append(columnLabels[i]);
			}
		}

		return sb.toString();
	}

	public void setColumnLabelsText(String columnLabelsText)
	{
		String[] texts = split(columnLabelsText);
		setColumnLabels(texts);
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
	protected String getDataSetMetaName()
	{
		return this.name;
	}

	protected String[] split(String texts)
	{
		texts = texts.trim();

		if (texts.startsWith(SPLITTER))
			texts = texts.substring(SPLITTER.length());
		if (texts.endsWith(SPLITTER))
			texts = texts.substring(0, texts.length() - SPLITTER.length());

		String[] array = texts.split(SPLITTER);
		for (int i = 0; i < array.length; i++)
			array[i] = array[i].trim();

		return array;
	}
}
