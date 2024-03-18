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

import org.springframework.beans.BeanUtils;

/**
 * 数据集资源目录实体。
 * <p>
 * 文件类数据集允许选择服务器端文件，需要限定用户可访问的服务端目录，此类即用于定义访问目录。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResDirectory extends AbstractStringIdEntity
		implements CreateUserEntity, DataPermissionEntity, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "DataSetDirectory";

	/**名称*/
	private String name;
	
	/** 目录 */
	private String directory;

	/** 描述 */
	private String desc = "";

	/** 此模式的创建用户 */
	private User createUser;

	/** 此模式的创建时间 */
	private Date createTime = null;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public DataSetResDirectory()
	{
		super();
	}

	public DataSetResDirectory(String id, String name, String directory, User createuUser)
	{
		super(id);
		this.name = name;
		this.directory = directory;
		this.createUser = createuUser;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
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
	public String toString()
	{
		return getClass().getSimpleName() + " [name="+name+", directory=" + directory + ", desc=" + desc + ", createUser=" + createUser
				+ ", createTime=" + createTime + ", dataPermission=" + dataPermission + "]";
	}

	@Override
	public DataSetResDirectory clone()
	{
		DataSetResDirectory entity = new DataSetResDirectory();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
