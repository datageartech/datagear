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
public class Schema extends AbstractStringIdEntity
		implements CreateUserEntity<String>, DataPermissionEntity<String>, Cloneable
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

	/** 数据库驱动程序路径名 */
	private DriverEntity driverEntity;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public Schema()
	{
		super();
	}

	public Schema(String id, String title, String url, String user, String password)
	{
		super(id);
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
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}

	/**
	 * 清除密码属性值。
	 * <p>
	 * 密码是敏感信息，某些情况下需要清除。
	 * </p>
	 * 
	 */
	public void clearPassword()
	{
		this.password = null;
	}

	public boolean canReadTableData()
	{
		return Authorization.canRead(this.dataPermission);
	}

	public boolean canEditTableData()
	{
		return Authorization.canEdit(this.dataPermission);
	}

	public boolean canDeleteTableData()
	{
		return Authorization.canDelete(this.dataPermission);
	}

	public boolean canRead()
	{
		return Authorization.canRead(this.dataPermission);
	}

	public boolean canEdit(User currentUser)
	{
		if (currentUser.isAdmin())
			return true;

		if (!Authorization.canEdit(this.dataPermission))
			return false;

		if (!this.hasCreateUser())
			return false;

		return currentUser.getId().equals(this.createUser.getId());
	}

	public boolean canDelete(User currentUser)
	{
		if (currentUser.isAdmin())
			return true;

		if (!Authorization.canRead(this.dataPermission))
			return false;

		if (!this.hasCreateUser())
			return false;

		return currentUser.getId().equals(this.createUser.getId());
	}

	public boolean canAuthorize(User currentUser)
	{
		if (currentUser.isAdmin())
			return true;

		if (currentUser.isAnonymous())
			return false;

		if (!Authorization.canDelete(this.dataPermission))
			return false;

		if (!this.hasCreateUser())
			return false;

		return currentUser.getId().equals(this.createUser.getId());
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [title=" + title + ", url=" + url + ", user=" + user + ", createUser="
				+ createUser + ", createTime=" + createTime + ", driverEntity=" + driverEntity + "]";
	}
}
