/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import org.datagear.model.support.AbstractStringIdEntity;

/**
 * 授权。
 * 
 * @author datagear@163.com
 *
 */
public class Authorization extends AbstractStringIdEntity implements CreateUserEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 授权源对象类型：数据源实体 */
	public static final String SOURCE_TYPE_DATA_SOURCE_ENTITY = "ST_DS_ENTITY";

	/** 授权源对象类型：数据源匹配模式 */
	public static final String SOURCE_TYPE_DATA_SOURCE_PATTERN = "ST_DS_PATTERN";

	/** 授权目标类型：用户 */
	public static final String TARGET_TYPE_USER = "USER";

	/** 授权目标类型：角色 */
	public static final String TARGET_TYPE_ROLE = "ROLE";

	/** 权限：无 */
	public static final String PERMISSION_NONE = "NONE";

	/** 权限：读 */
	public static final String PERMISSION_READ = "READ";

	/** 权限：写 */
	public static final String PERMISSION_WRITE = "WRITE";

	/** 授权源对象 */
	private String source;

	/** 授权源对象类型 */
	private String sourceType;

	/** 授权目标 */
	private String target;

	/** 授权目标类型 */
	private String targetType;

	/** 权限 */
	private String permission;

	/** 是否已禁用 */
	private boolean disabled = false;

	/** 授权创建用户 */
	private User createUser;

	public Authorization()
	{
		super();
	}

	public Authorization(String id, String source, String sourceType, String target, String targetType,
			String permission, User createUser)
	{
		super(id);
		this.source = source;
		this.sourceType = sourceType;
		this.target = target;
		this.targetType = targetType;
		this.permission = permission;
		this.createUser = createUser;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getSourceType()
	{
		return sourceType;
	}

	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getTargetType()
	{
		return targetType;
	}

	public void setTargetType(String targetType)
	{
		this.targetType = targetType;
	}

	public String getPermission()
	{
		return permission;
	}

	public void setPermission(String permission)
	{
		this.permission = permission;
	}

	public boolean isPermissionNone()
	{
		return PERMISSION_NONE.equals(this.permission);
	}

	public boolean isPermissionRead()
	{
		return PERMISSION_READ.equals(this.permission);
	}

	public boolean isPermissionWrite()
	{
		return PERMISSION_WRITE.equals(this.permission);
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
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
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + getId() + ", source=" + source + ", sourceType=" + sourceType
				+ ", target=" + target + ", targetType=" + targetType + ", permission=" + permission + "]";
	}
}
