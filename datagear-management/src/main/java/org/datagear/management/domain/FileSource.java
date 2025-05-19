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
 * 文件源。
 * <p>
 * 文件源对应一个服务器端目录，文件类数据集可以选择和使用文件源对应目录内的文件。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FileSource extends AbstractStringIdEntity
		implements CreateUserEntity, DataPermissionEntity, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "FileSource";

	/**名称*/
	private String name;
	
	/** 目录 */
	private String directory;

	/** 描述 */
	private String description = "";

	/** 此模式的创建用户 */
	private User createUser;

	/** 此模式的创建时间 */
	private Date createTime = null;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public FileSource()
	{
		super();
	}

	public FileSource(String id, String name, String directory, User createuUser)
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
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
		return getClass().getSimpleName() + " [name=" + name + ", directory=" + directory + ", description="
				+ description + ", createUser=" + createUser + ", createTime=" + createTime + ", dataPermission="
				+ dataPermission + "]";
	}

	@Override
	public FileSource clone()
	{
		FileSource entity = new FileSource();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
