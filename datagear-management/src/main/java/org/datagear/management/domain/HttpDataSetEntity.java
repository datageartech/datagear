/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.classic.HttpClient;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.HttpDataSet;

/**
 * {@linkplain HttpDataSet}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HttpDataSetEntity extends HttpDataSet implements DataSetEntity
{
	private static final long serialVersionUID = 1L;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private AnalysisProject analysisProject = null;

	public HttpDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public HttpDataSetEntity(String id, String name, HttpClient httpClient, String uri, User createUser)
	{
		super(id, name, httpClient, uri);
		this.createTime = new Date();
		this.createUser = createUser;
	}

	public HttpDataSetEntity(String id, String name, List<DataSetProperty> properties, HttpClient httpClient,
			String uri, User createUser)
	{
		super(id, name, properties, httpClient, uri);
		this.createTime = new Date();
		this.createUser = createUser;
	}

	@Override
	public String getDataSetType()
	{
		return DataSetEntity.DATA_SET_TYPE_Http;
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
	public AnalysisProject getAnalysisProject()
	{
		return analysisProject;
	}

	@Override
	public void setAnalysisProject(AnalysisProject analysisProject)
	{
		this.analysisProject = analysisProject;
	}
}
