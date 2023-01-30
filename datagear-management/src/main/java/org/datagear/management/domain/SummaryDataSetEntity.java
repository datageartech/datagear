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

package org.datagear.management.domain;

import java.util.Collections;
import java.util.Date;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.AbstractDataSet;
import org.springframework.beans.BeanUtils;

/**
 * 概要{@linkplain DataSetEntity}。
 * <p>
 * 此类不表示任何具体的{@linkplain DataSet}实现，仅用于表示{@linkplain DataSetEntity}结构。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SummaryDataSetEntity extends AbstractDataSet implements DataSetEntity, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	private String dataSetType;

	private User createUser;

	private Date createTime;

	private int dataPermission = PERMISSION_NOT_LOADED;

	private AnalysisProject analysisProject = null;

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
	public AnalysisProject getAnalysisProject()
	{
		return analysisProject;
	}

	@Override
	public void setAnalysisProject(AnalysisProject analysisProject)
	{
		this.analysisProject = analysisProject;
	}

	@Override
	public DataSetResult getResult(DataSetQuery query) throws DataSetException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SummaryDataSetEntity clone()
	{
		SummaryDataSetEntity entity = new SummaryDataSetEntity();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
