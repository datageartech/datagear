/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
		implements CreateUserEntity<String>, DataPermissionEntity<String>, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "DataSetDirectory";

	/** 目录 */
	private String directory;

	/** 描述 */
	private String desc = "";

	/** 此模式的创建用户 */
	private User createUser;

	/** 此模式的创建时间 */
	private Date createTime = new Date();

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public DataSetResDirectory()
	{
		super();
	}

	public DataSetResDirectory(String id, String directory, User createuUser)
	{
		super(id);
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
		return getClass().getSimpleName() + " [directory=" + directory + ", desc=" + desc + ", createUser=" + createUser
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
