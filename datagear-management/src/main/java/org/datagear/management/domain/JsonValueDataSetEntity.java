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
import org.datagear.analysis.support.JsonValueDataSet;

/**
 * {@linkplain JsonValueDataSet}实体。
 * 
 * @author datagear@163.com
 *
 */
public class JsonValueDataSetEntity extends JsonValueDataSet implements DataSetEntity
{
	private static final long serialVersionUID = 1L;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private transient String _propertyLabelsText;

	public JsonValueDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public JsonValueDataSetEntity(String id, String name, List<DataSetProperty> properties, String value,
			User createUser)
	{
		super(id, name, properties, value);
		this.createTime = new Date();
		this.createUser = createUser;
	}

	@Override
	public String getDataSetType()
	{
		return DataSetEntity.DATA_SET_TYPE_JSON_VALUE;
	}

	@Override
	public void setDataSetType(String dataSetType)
	{
		// XXX 什么也不做，不采用抛出异常的方式，便于统一底层SQL查询语句
		// throw new UnsupportedOperationException();
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
			DataSetEntity.setPropertyLabelsText(properties, this._propertyLabelsText);
	}

	@Override
	public void setPropertyLabelsText(String text)
	{
		List<DataSetProperty> properties = getProperties();
		
		if(properties == null || properties.isEmpty())
			this._propertyLabelsText = text;
		else
		{
			this._propertyLabelsText = null;
			DataSetEntity.setPropertyLabelsText(properties, text);
		}
	}

	@Override
	public String getPropertyLabelsText()
	{
		return DataSetEntity.getPropertyLabelsText(getProperties());
	}
}
