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

package org.datagear.web.controller;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.management.domain.Authorization;

/**
 * 授权资源元信息管理器。
 * <p>
 * {@linkplain AuthorizationController}使用此类提供的元信息绘制授权页面。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationResMetaManager
{
	private final ConcurrentMap<String, ResourceMeta> resourcemetaMap = new ConcurrentHashMap<String, ResourceMeta>();

	public AuthorizationResMetaManager()
	{
		super();
	}

	/**
	 * 注册{@linkplain ResourceMeta}。
	 * 
	 * @param resourceMeta
	 */
	public void register(ResourceMeta resourceMeta)
	{
		resourcemetaMap.put(resourceMeta.getResourceType(), resourceMeta);
	}

	/**
	 * 注册用于支持分享功能的{@linkplain ResourceMeta}。
	 * 
	 * @param resourceType
	 */
	public void registerForShare(String resourceType)
	{
		PermissionMeta read = PermissionMeta.valueOfRead();
		ResourceMeta resourceMeta = new ResourceMeta(resourceType, PermissionMeta.valuesOf(read));
		resourceMeta.setEnableSetEnable(false);
		resourceMeta.setAuthModuleLabel("module.share");

		resourceMeta.setAuthPrincipalLabel("authorization.default.share.principal");
		resourceMeta.setAuthPrincipalTypeLabel("authorization.default.share.principalType");

		register(resourceMeta);
	}
	
	/**
	 * 注册标准的【读、写、删除、无】授权功能{@linkplain ResourceMeta}。
	 * 
	 * @param resourceType
	 */
	public void registerForStandard(String resourceType)
	{
		PermissionMeta read = PermissionMeta.valueOfRead();
		PermissionMeta edit = PermissionMeta.valueOfEdit();
		PermissionMeta delete = PermissionMeta.valueOfDelete();
		PermissionMeta none = PermissionMeta.valueOfNone();

		ResourceMeta resourceMeta = new ResourceMeta(resourceType, PermissionMeta.valuesOf(read, edit, delete, none));

		register(resourceMeta);
	}

	/**
	 * 获取{@linkplain ResourceMeta}。
	 * <p>
	 * 没有，则返回{@code null}。
	 * </p>
	 * 
	 * @param resourceType
	 * @return
	 */
	public ResourceMeta get(String resourceType)
	{
		return resourcemetaMap.get(resourceType);
	}
	
	/**
	 * 授权资源元信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ResourceMeta implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 资源类型 */
		private String resourceType;

		/** 资源权限元信息 */
		private PermissionMeta[] permissionMetas;

		/** 是否开启设置启用/禁用功能 */
		private boolean enableSetEnable = true;

		private String authModuleLabel = "module.authorization";
		
		private String authResourceLabel = "authorization.resource";

		private String authResourceTypeLabel = "authorization.resourceType";

		private String authPrincipalLabel = "authorization.principal";

		private String authPrincipalTypeLabel = "authorization.principalType";

		private String authPermissionLabel = "authorization.permission";

		private String authEnabledLabel = "isEnabled";

		public ResourceMeta()
		{
			super();
		}

		public ResourceMeta(String resourceType, PermissionMeta... permissionMetas)
		{
			super();
			this.resourceType = resourceType;
			this.permissionMetas = permissionMetas;
		}

		public String getResourceType()
		{
			return resourceType;
		}

		public void setResourceType(String resourceType)
		{
			this.resourceType = resourceType;
		}

		public PermissionMeta[] getPermissionMetas()
		{
			return permissionMetas;
		}

		public void setPermissionMetas(PermissionMeta[] permissionMetas)
		{
			this.permissionMetas = permissionMetas;
		}

		public boolean isEnableSetEnable()
		{
			return enableSetEnable;
		}

		public void setEnableSetEnable(boolean enableSetEnable)
		{
			this.enableSetEnable = enableSetEnable;
		}

		public String getAuthModuleLabel()
		{
			return authModuleLabel;
		}

		public void setAuthModuleLabel(String authModuleLabel)
		{
			this.authModuleLabel = authModuleLabel;
		}

		public String getAuthResourceLabel()
		{
			return authResourceLabel;
		}

		public void setAuthResourceLabel(String authResourceLabel)
		{
			this.authResourceLabel = authResourceLabel;
		}

		public String getAuthResourceTypeLabel()
		{
			return authResourceTypeLabel;
		}

		public void setAuthResourceTypeLabel(String authResourceTypeLabel)
		{
			this.authResourceTypeLabel = authResourceTypeLabel;
		}

		public String getAuthPrincipalLabel()
		{
			return authPrincipalLabel;
		}

		public void setAuthPrincipalLabel(String authPrincipalLabel)
		{
			this.authPrincipalLabel = authPrincipalLabel;
		}

		public String getAuthPrincipalTypeLabel()
		{
			return authPrincipalTypeLabel;
		}

		public void setAuthPrincipalTypeLabel(String authPrincipalTypeLabel)
		{
			this.authPrincipalTypeLabel = authPrincipalTypeLabel;
		}

		public String getAuthPermissionLabel()
		{
			return authPermissionLabel;
		}

		public void setAuthPermissionLabel(String authPermissionLabel)
		{
			this.authPermissionLabel = authPermissionLabel;
		}

		public String getAuthEnabledLabel()
		{
			return authEnabledLabel;
		}

		public void setAuthEnabledLabel(String authEnabledLabel)
		{
			this.authEnabledLabel = authEnabledLabel;
		}

		/**
		 * 是否只有一个权限。
		 * 
		 * @return
		 */
		public boolean isSinglePermission()
		{
			return (this.permissionMetas != null && this.permissionMetas.length == 1);
		}

		public PermissionMeta getSinglePermissionMeta()
		{
			return this.permissionMetas[0];
		}
	}

	/**
	 * 授权资源权限值元信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class PermissionMeta implements Serializable
	{
		public static final String[] DEFAULT_SUB_LABELS = { "READ", "EDIT", "DELETE", "NONE" };

		private static final long serialVersionUID = 1L;

		/** 权限值 */
		private int permission;

		/** 权限标签I18N关键字 */
		private String permissionLabel;

		/** 可选，权限标签描述I18N关键字 */
		private String permissionLabelDesc = "authorization.default.permission.desc";

		public PermissionMeta()
		{
			super();
		}

		public PermissionMeta(int permission, String permissionLabel)
		{
			super();
			this.permission = permission;
			this.permissionLabel = permissionLabel;
		}

		public int getPermission()
		{
			return permission;
		}

		public void setPermission(int permission)
		{
			this.permission = permission;
		}

		public String getPermissionLabel()
		{
			return permissionLabel;
		}

		public void setPermissionLabel(String permissionLabel)
		{
			this.permissionLabel = permissionLabel;
		}

		public String getPermissionLabelDesc()
		{
			return permissionLabelDesc;
		}

		public void setPermissionLabelDesc(String permissionLabelDesc)
		{
			this.permissionLabelDesc = permissionLabelDesc;
		}

		public static PermissionMeta valueOf(int permission, String permissionLabel)
		{
			return new PermissionMeta(permission, permissionLabel);
		}

		public static PermissionMeta valueOfRead()
		{
			return valueOfRead(Authorization.PERMISSION_READ_START);
		}

		public static PermissionMeta valueOfRead(int permission)
		{
			PermissionMeta pm = new PermissionMeta(permission, "authorization.permission.READ");
			pm.setPermissionLabelDesc("authorization.permission.READ.desc");
			
			return pm;
		}

		public static PermissionMeta valueOfEdit()
		{
			return valueOfEdit(Authorization.PERMISSION_EDIT_START);
		}

		public static PermissionMeta valueOfEdit(int permission)
		{
			PermissionMeta pm = new PermissionMeta(permission, "authorization.permission.EDIT");
			pm.setPermissionLabelDesc("authorization.permission.EDIT.desc");
			
			return pm;
		}

		public static PermissionMeta valueOfDelete()
		{
			return valueOfDelete(Authorization.PERMISSION_DELETE_START);
		}

		public static PermissionMeta valueOfDelete(int permission)
		{
			PermissionMeta pm = new PermissionMeta(permission, "authorization.permission.DELETE");
			pm.setPermissionLabelDesc("authorization.permission.DELETE.desc");
			
			return pm;
		}

		public static PermissionMeta valueOfNone()
		{
			return valueOfNone(Authorization.PERMISSION_NONE_START);
		}

		public static PermissionMeta valueOfNone(int permission)
		{
			PermissionMeta pm = new PermissionMeta(permission, "authorization.permission.NONE");
			pm.setPermissionLabelDesc("authorization.permission.NONE.desc");
			
			return pm;
		}

		public static PermissionMeta[] valuesOf(PermissionMeta... permissionMetas)
		{
			return permissionMetas;
		}
	}
}