/*
 * Copyright 2018-2024 datagear.tech
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

import org.springframework.beans.BeanUtils;

/**
 * 数据分析项目实体。
 * 
 * @author datagear@163.com
 *
 */
public class AnalysisProject extends AbstractStringIdEntity
		implements CreateUserEntity<String>, DataPermissionEntity<String>, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "AnalysisProject";

	/** 名称 */
	private String name;

	/** 描述 */
	private String desc = "";

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime = new Date();

	private int dataPermission = PERMISSION_NOT_LOADED;

	public AnalysisProject()
	{
		super();
	}

	public AnalysisProject(String id, String name, User createUser)
	{
		super(id);
		this.name = name;
		this.createUser = createUser;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
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
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", desc=" + desc + ", createUser=" + createUser
				+ ", createTime=" + createTime + ", dataPermission=" + dataPermission + "]";
	}

	@Override
	public AnalysisProject clone()
	{
		AnalysisProject entity = new AnalysisProject();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
