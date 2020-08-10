/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.AbstractDataSet;

/**
 * 概要数据集实体。
 * 
 * @author datagear@163.com
 *
 */
public class SummaryDataSetEntity extends AbstractDataSet implements DataSetEntity
{
	private static final long serialVersionUID = 1L;

	private String dataSetType;

	private User createUser;

	private Date createTime;

	private int dataPermission = PERMISSION_NOT_LOADED;

	public SummaryDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	@SuppressWarnings("unchecked")
	public SummaryDataSetEntity(String id, String name, String dataSetType, User createUser)
	{
		super(id, name, Collections.EMPTY_LIST);
		this.dataSetType = dataSetType;
		this.createTime = new Date();
		this.createUser = createUser;
	}

	@Override
	public String getDataSetType()
	{
		return this.dataSetType;
	}

	@Override
	public void setDataSetType(String dataSetType)
	{
		this.dataSetType = dataSetType;
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
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		throw new UnsupportedOperationException();
	}
}
