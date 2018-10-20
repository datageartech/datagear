/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.util.Date;

import org.datagear.connection.DriverEntity;
import org.datagear.model.support.AbstractStringIdEntity;

/**
 * 数据库模式实体。
 * 
 * @author datagear@163.com
 *
 */
public class Schema extends AbstractStringIdEntity implements CreateUserEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 标题 */
	private String title;

	/** 连接URL */
	private String url;

	/** 连接用户 */
	private String user;

	/** 连接密码 */
	private String password;

	/** 此模式的创建用户 */
	private User createUser;

	/** 此模式的创建时间 */
	private Date createTime;

	/** 是否共享的 */
	private boolean shared = false;

	/** 数据库驱动程序路径名 */
	private DriverEntity driverEntity;

	public Schema()
	{
		super();
	}

	public Schema(String id, String title, String url, String user, String password)
	{
		super();
		this.title = title;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean hasCreateUser()
	{
		return (this.createUser != null);
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

	public boolean hasCreateTime()
	{
		return (this.createTime != null);
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	public boolean isShared()
	{
		return shared;
	}

	public void setShared(boolean shared)
	{
		this.shared = shared;
	}

	public boolean hasDriverEntity()
	{
		if (this.driverEntity == null)
			return false;

		String driverEntityId = this.driverEntity.getId();

		return (driverEntityId != null && !driverEntityId.isEmpty());
	}

	public DriverEntity getDriverEntity()
	{
		return driverEntity;
	}

	public void setDriverEntity(DriverEntity driverEntity)
	{
		this.driverEntity = driverEntity;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [title=" + title + ", url=" + url + ", user=" + user + ", password="
				+ password + ", createUser=" + createUser + ", createTime=" + createTime + ", shared=" + shared
				+ ", driverEntity=" + driverEntity + "]";
	}
}
