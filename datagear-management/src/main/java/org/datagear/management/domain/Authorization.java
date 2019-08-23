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

	/** 授权资源类型：数据源实体 */
	public static final String RESOURCE_TYPE_DATA_SOURCE_ID = "DATA_SOURCE_ID";

	/** 授权资源类型：数据源通配符 */
	public static final String RESOURCE_TYPE_DATA_SOURCE_PATTERN = "DATA_SOURCE_PATTERN";

	/** 授权主体类型：全部用户 */
	public static final String PRINCIPAL_TYPE_ALl = "ALL";

	/** 授权主体类型：角色ID */
	public static final String PRINCIPAL_TYPE_ROLE = "ROLE";

	/** 授权主体类型：用户ID */
	public static final String PRINCIPAL_TYPE_USER = "USER";

	/** 授权主体类型：匿名用户 */
	public static final String PRINCIPAL_TYPE_ANONYMOUS = "ANONYMOUS";

	/** 授权主体：匿名用户 */
	public static final String PRINCIPAL_ANONYMOUS = "anonymous";

	/** 授权主体：全部用户 */
	public static final String PRINCIPAL_ALL = "all";

	/** 权限：无 */
	public static final int PERMISSION_NONE = 0;

	/** 权限：读 */
	public static final int PERMISSION_READ = 1;

	/** 权限：写 */
	public static final int PERMISSION_WRITE = 2;

	/** 授权资源 */
	private String resource;

	/** 授权资源类型 */
	private String resourceType;

	/** 授权主体 */
	private String principal;

	/** 授权主体类型 */
	private String principalType;

	/** 权限 */
	private int permission;

	/** 是否启用 */
	private boolean enabled = true;

	/** 授权创建用户 */
	private User createUser;

	/** 授权资源名称 */
	private String resourceName;

	/** 授权主体名称 */
	private String principalName;

	public Authorization()
	{
		super();
	}

	public Authorization(String resource, String resourceType, String principal, String principalType, int permission,
			User createUser)
	{
		super();
		this.resource = resource;
		this.resourceType = resourceType;
		this.principal = principal;
		this.principalType = principalType;
		this.permission = permission;
		this.createUser = createUser;
	}

	public String getResource()
	{
		return resource;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	public String getResourceType()
	{
		return resourceType;
	}

	public void setResourceType(String resourceType)
	{
		this.resourceType = resourceType;
	}

	public String getPrincipal()
	{
		return principal;
	}

	public void setPrincipal(String principal)
	{
		this.principal = principal;
	}

	public String getPrincipalType()
	{
		return principalType;
	}

	public void setPrincipalType(String principalType)
	{
		this.principalType = principalType;
	}

	public int getPermission()
	{
		return permission;
	}

	public void setPermission(int permission)
	{
		this.permission = permission;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
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

	public String getResourceName()
	{
		return resourceName;
	}

	public void setResourceName(String resourceName)
	{
		this.resourceName = resourceName;
	}

	public String getPrincipalName()
	{
		return principalName;
	}

	public void setPrincipalName(String principalName)
	{
		this.principalName = principalName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + getId() + ", resource=" + resource + ", resourceType="
				+ resourceType + ", principal=" + principal + ", principalType=" + principalType + ", permission="
				+ permission + ", enabled=" + enabled + "]";
	}
}
