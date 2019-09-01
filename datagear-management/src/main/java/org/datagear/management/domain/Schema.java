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

	/*------------------------------------------------------*/
	/*
	 * 从业务角度看，对数据源的授权不应是对其记录本身，而是它包含表中的数据。
	 * 所以，这里扩展了Authorization.PERMISSION_READ_START权限，授予下面这些权限，都是对数据源记录本身的读权限。
	 */

	/** 数据源内的表数据权限：读取 */
	public static final int PERMISSION_TABLE_DATA_READ = Authorization.PERMISSION_READ_START;

	/** 数据源内的表数据权限：编辑 */
	public static final int PERMISSION_TABLE_DATA_EDIT = Authorization.PERMISSION_READ_START + 4;

	/** 数据源内的表数据权限：删除 */
	public static final int PERMISSION_TABLE_DATA_DELETE = Authorization.PERMISSION_READ_START + 8;

	/*------------------------------------------------------*/

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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [title=" + title + ", url=" + url + ", user=" + user + ", createUser="
				+ createUser + ", createTime=" + createTime + ", driverEntity=" + driverEntity + "]";
	}
}
