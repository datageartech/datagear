/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

/**
 * 授权。
 * 
 * @author datagear@163.com
 *
 */
public class Authorization extends AbstractStringIdEntity
		implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "AUTHORIZATION";

	/**
	 * 模式匹配资源类型的后缀，例如："DATA_SOURCE_PATTERN"，表示数据源资源模式匹配授权
	 */
	public static final String PATTERN_RESOURCE_TYPE_SUFFIX = "_PATTERN";

	/** 授权主体类型：全部用户 */
	public static final String PRINCIPAL_TYPE_ALL = "ALL";

	/** 授权主体类型：角色ID */
	public static final String PRINCIPAL_TYPE_ROLE = "ROLE";

	/** 授权主体类型：用户ID */
	public static final String PRINCIPAL_TYPE_USER = "USER";

	/** 授权主体类型：匿名用户 */
	public static final String PRINCIPAL_TYPE_ANONYMOUS = "ANONYMOUS";

	/** 授权主体：全部用户 */
	public static final String PRINCIPAL_ALL = "all";

	/** 授权主体：匿名用户 */
	public static final String PRINCIPAL_ANONYMOUS = "anonymous";

	/*------------------------------------------------------*/
	/*
	 * 注意：权限值范围必须在[0, 99]之间，因为commonDataPermissionSqls.xml会对权限值取模100。
	 * 这里的权限值都留有间隔，便于各模块扩展自定义权限值。
	 */

	/** 权限起始值：无 */
	public static final int PERMISSION_NONE_START = 0;

	/** 权限起始值：只读 */
	public static final int PERMISSION_READ_START = 20;

	/** 权起始值限：编辑 */
	public static final int PERMISSION_EDIT_START = 40;

	/** 权限起始值：删除 */
	public static final int PERMISSION_DELETE_START = 60;

	/** 最大权限值 */
	public static final int PERMISSION_MAX = 99;

	/*------------------------------------------------------*/

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

	/** 权限标签 */
	private String permissionLabel;

	/** 此记录的数据权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

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

	public String getPermissionLabel()
	{
		return permissionLabel;
	}

	public void setPermissionLabel(String permissionLabel)
	{
		this.permissionLabel = permissionLabel;
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
		return getClass().getSimpleName() + " [id=" + getId() + ", resource=" + resource + ", resourceType="
				+ resourceType + ", principal=" + principal + ", principalType=" + principalType + ", permission="
				+ permission + ", enabled=" + enabled + "]";
	}

	/**
	 * 是否是模式匹配资源类型。
	 * 
	 * @return
	 */
	public boolean isResourceTypePattern()
	{
		return this.resourceType != null && this.resourceType.endsWith(PATTERN_RESOURCE_TYPE_SUFFIX);
	}

	/**
	 * 是否无权限。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean isNone(int permission)
	{
		return (permission >= PERMISSION_NONE_START && permission < PERMISSION_READ_START);
	}

	/**
	 * 是否是只读权限。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean isRead(int permission)
	{
		return (permission >= PERMISSION_READ_START && permission < PERMISSION_EDIT_START);
	}

	/**
	 * 是否是可编辑权限。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean isEdit(int permission)
	{
		return (permission >= PERMISSION_EDIT_START && permission < PERMISSION_DELETE_START);
	}

	/**
	 * 是否是可删除权限。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean isDelete(int permission)
	{
		return (permission >= PERMISSION_DELETE_START);
	}

	/**
	 * 是否可读、或者可编辑、或者可删除。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean canRead(int permission)
	{
		return (permission >= PERMISSION_READ_START);
	}

	/**
	 * 是否可编辑、或者可删除。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean canEdit(int permission)
	{
		return (permission >= PERMISSION_EDIT_START);
	}

	/**
	 * 是否可删除。
	 * 
	 * @param permission
	 * @return
	 */
	public static boolean canDelete(int permission)
	{
		return (permission >= PERMISSION_DELETE_START);
	}

	/**
	 * 是否可授权。
	 * <p>
	 * {@code currentUser}必须是用于查询{@code entity}的用户。
	 * </p>
	 * 
	 * @param entity
	 * @param currentUser
	 * @return
	 */
	public static boolean canAuthorize(DataPermissionEntity<?> entity, User currentUser)
	{
		if (currentUser.isAdmin())
			return true;

		if (currentUser.isAnonymous())
			return false;

		if (!Authorization.canDelete(entity.getDataPermission()))
			return false;

		if (!(entity instanceof CreateUserEntity<?>))
			return false;

		CreateUserEntity<?> createUserEntity = (CreateUserEntity<?>) entity;

		if (createUserEntity.getCreateUser() == null)
			return false;

		return currentUser.getId().equals(createUserEntity.getCreateUser().getId());
	}
}
