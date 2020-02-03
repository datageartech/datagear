/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.Schema;

/**
 * 授权资源元信息。
 * <p>
 * {@linkplain AuthorizationController}使用此类提供的元信息绘制授权页面。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationResourceMetas
{
	private static final ConcurrentMap<String, ResourceMeta> RESOURCEMETA_MAP = new ConcurrentHashMap<String, ResourceMeta>();

	/**
	 * 注册{@linkplain ResourceMeta}。
	 * 
	 * @param resourceMeta
	 */
	public static void register(ResourceMeta resourceMeta)
	{
		RESOURCEMETA_MAP.put(resourceMeta.getResourceType(), resourceMeta);
	}

	/**
	 * 注册用于支持分享功能的{@linkplain ResourceMeta}。
	 * 
	 * @param resourceType
	 * @param labelKeyPrefix
	 */
	public static void registerForShare(String resourceType, String labelKeyPrefix)
	{
		PermissionMeta read = PermissionMeta.valueOfRead();
		ResourceMeta resourceMeta = new ResourceMeta(resourceType, labelKeyPrefix, PermissionMeta.valuesOf(read));
		resourceMeta.updateResouceTypeLabel();
		resourceMeta.setSupportSelectResource(false);
		resourceMeta.setSupportPatternResource(false);
		resourceMeta.setEnableSetEnable(false);

		resourceMeta.setAuthManageAuthorizationLabel("authorization.default.share.manageAuthorization");
		resourceMeta.setAuthAddAuthorizationLabel("authorization.default.share.addAuthorization");
		resourceMeta.setAuthEditAuthorizationLabel("authorization.default.share.editAuthorization");
		resourceMeta.setAuthViewAuthorizationLabel("authorization.default.share.viewAuthorization");
		resourceMeta.setAuthPrincipalTypeLabel("authorization.default.share.principalType");
		resourceMeta.setAuthPrincipalLabel("authorization.default.share.principal");

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
	public static ResourceMeta get(String resourceType)
	{
		return RESOURCEMETA_MAP.get(resourceType);
	}

	static
	{
		// 数据源授权资源元信息
		{
			PermissionMeta read = PermissionMeta.valueOfRead(Schema.PERMISSION_TABLE_DATA_READ);
			read.setPermissionLabelDesc(ResourceMeta.buildLabelKey("schema", "permission.read.desc"));

			PermissionMeta edit = PermissionMeta.valueOfEdit(Schema.PERMISSION_TABLE_DATA_EDIT);
			edit.setPermissionLabelDesc(ResourceMeta.buildLabelKey("schema", "permission.edit.desc"));

			PermissionMeta delete = PermissionMeta.valueOfDelete(Schema.PERMISSION_TABLE_DATA_DELETE);
			delete.setPermissionLabelDesc(ResourceMeta.buildLabelKey("schema", "permission.delete.desc"));

			PermissionMeta none = PermissionMeta.valueOfNone();
			none.setPermissionLabelDesc(ResourceMeta.buildLabelKey("schema", "permission.none.desc"));

			ResourceMeta resourceMeta = new ResourceMeta(Schema.AUTHORIZATION_RESOURCE_TYPE, "schema",
					PermissionMeta.valuesOf(read, edit, delete, none));
			resourceMeta.updateResouceTypeLabel();
			resourceMeta.setSupportSelectResource(true);
			resourceMeta.setSelectResourceURL("/schema/select");
			resourceMeta.setSelectResourceIdField(Schema.ID_PROP_NAME);
			resourceMeta.setSelectResourceNameField(Schema.PROPERTY_TITLE);
			resourceMeta.updateAuthModeSelectResourceLabel();
			resourceMeta.setSupportPatternResource(true);
			resourceMeta.updateAuthModePatternResourceLabel();
			resourceMeta.updateAuthModePatternResourceLabelDesc();

			register(resourceMeta);
		}
	}

	private AuthorizationResourceMetas()
	{
		super();
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

		/** 标签I18N关键字前缀 */
		private String labelKeyPrefix;

		/** 资源权限元信息 */
		private PermissionMeta[] permissionMetas;

		/** 资源类型标签I18N关键字 */
		private String resouceTypeLabel = "authorization.default.resouceTypeLabel";

		/** 是否支持选择资源授权 */
		private boolean supportSelectResource = false;

		/** supportSelectResource=true时必须，选择资源URL */
		private String selectResourceURL = "";

		/** supportSelectResource=true时必须，选择资源的ID字段名 */
		private String selectResourceIdField = "";

		/** supportSelectResource=true时必须，选择资源的名称字段名 */
		private String selectResourceNameField = "";

		/** 是否支持模式匹配授权 */
		private boolean supportPatternResource = false;

		/** 可选，授权模式-选择资源选项卡标签名I18N关键字 */
		private String authModeSelectResourceLabel = "authorization.default.authModeSelectResourceLabel";

		/** 可选，授权模式-选择资源选项卡标签描述I18N关键字 */
		private String authModeSelectResourceLabelDesc = "authorization.default.authModeSelectResourceLabelDesc";

		/** 可选，授权模式-模式输入选项卡标签I18N关键字 */
		private String authModePatternResourceLabel = "authorization.default.authModePatternResourceLabel";

		/** 可选，授权模式-模式输入选项卡标签描述I18N关键字 */
		private String authModePatternResourceLabelDesc = "authorization.default.authModePatternResourceLabelDesc";

		/** 是否开启设置启用/禁用功能 */
		private boolean enableSetEnable = true;

		private String authManageAuthorizationLabel = "authorization.manageAuthorization";

		private String authAddAuthorizationLabel = "authorization.addAuthorization";

		private String authEditAuthorizationLabel = "authorization.editAuthorization";

		private String authViewAuthorizationLabel = "authorization.viewAuthorization";

		private String authResourceLabel = "authorization.resource";

		private String authResourceTypeLabel = "authorization.resourceType";

		private String authPrincipalLabel = "authorization.principal";

		private String authPrincipalTypeLabel = "authorization.principalType";

		private String authPermissionLabel = "authorization.permission";

		private String authEnabledLabel = "authorization.enabled";

		private String authCreateUserLabel = "authorization.createUser";

		public ResourceMeta()
		{
			super();
		}

		public ResourceMeta(String resourceType, String labelKeyPrefix, PermissionMeta... permissionMetas)
		{
			super();
			this.resourceType = resourceType;
			this.labelKeyPrefix = labelKeyPrefix;
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

		public String getLabelKeyPrefix()
		{
			return labelKeyPrefix;
		}

		public void setLabelKeyPrefix(String labelKeyPrefix)
		{
			this.labelKeyPrefix = labelKeyPrefix;
		}

		public PermissionMeta[] getPermissionMetas()
		{
			return permissionMetas;
		}

		public void setPermissionMetas(PermissionMeta[] permissionMetas)
		{
			this.permissionMetas = permissionMetas;
		}

		public String getResouceTypeLabel()
		{
			return resouceTypeLabel;
		}

		public void setResouceTypeLabel(String resouceTypeLabel)
		{
			this.resouceTypeLabel = resouceTypeLabel;
		}

		public boolean isSupportSelectResource()
		{
			return supportSelectResource;
		}

		public void setSupportSelectResource(boolean supportSelectResource)
		{
			this.supportSelectResource = supportSelectResource;
		}

		public String getSelectResourceURL()
		{
			return selectResourceURL;
		}

		public void setSelectResourceURL(String selectResourceURL)
		{
			this.selectResourceURL = selectResourceURL;
		}

		public String getSelectResourceIdField()
		{
			return selectResourceIdField;
		}

		public void setSelectResourceIdField(String selectResourceIdField)
		{
			this.selectResourceIdField = selectResourceIdField;
		}

		public String getSelectResourceNameField()
		{
			return selectResourceNameField;
		}

		public void setSelectResourceNameField(String selectResourceNameField)
		{
			this.selectResourceNameField = selectResourceNameField;
		}

		public String getAuthModeSelectResourceLabel()
		{
			return authModeSelectResourceLabel;
		}

		public void setAuthModeSelectResourceLabel(String authModeSelectResourceLabel)
		{
			this.authModeSelectResourceLabel = authModeSelectResourceLabel;
		}

		public String getAuthModeSelectResourceLabelDesc()
		{
			return authModeSelectResourceLabelDesc;
		}

		public void setAuthModeSelectResourceLabelDesc(String authModeSelectResourceLabelDesc)
		{
			this.authModeSelectResourceLabelDesc = authModeSelectResourceLabelDesc;
		}

		public boolean isSupportPatternResource()
		{
			return supportPatternResource;
		}

		public void setSupportPatternResource(boolean supportPatternResource)
		{
			this.supportPatternResource = supportPatternResource;
		}

		public String getAuthModePatternResourceLabel()
		{
			return authModePatternResourceLabel;
		}

		public void setAuthModePatternResourceLabel(String authModePatternResourceLabel)
		{
			this.authModePatternResourceLabel = authModePatternResourceLabel;
		}

		public String getAuthModePatternResourceLabelDesc()
		{
			return authModePatternResourceLabelDesc;
		}

		public void setAuthModePatternResourceLabelDesc(String authModePatternResourceLabelDesc)
		{
			this.authModePatternResourceLabelDesc = authModePatternResourceLabelDesc;
		}

		public boolean isEnableSetEnable()
		{
			return enableSetEnable;
		}

		public void setEnableSetEnable(boolean enableSetEnable)
		{
			this.enableSetEnable = enableSetEnable;
		}

		public String getAuthManageAuthorizationLabel()
		{
			return authManageAuthorizationLabel;
		}

		public void setAuthManageAuthorizationLabel(String authManageAuthorizationLabel)
		{
			this.authManageAuthorizationLabel = authManageAuthorizationLabel;
		}

		public String getAuthAddAuthorizationLabel()
		{
			return authAddAuthorizationLabel;
		}

		public void setAuthAddAuthorizationLabel(String authAddAuthorizationLabel)
		{
			this.authAddAuthorizationLabel = authAddAuthorizationLabel;
		}

		public String getAuthEditAuthorizationLabel()
		{
			return authEditAuthorizationLabel;
		}

		public void setAuthEditAuthorizationLabel(String authEditAuthorizationLabel)
		{
			this.authEditAuthorizationLabel = authEditAuthorizationLabel;
		}

		public String getAuthViewAuthorizationLabel()
		{
			return authViewAuthorizationLabel;
		}

		public void setAuthViewAuthorizationLabel(String authViewAuthorizationLabel)
		{
			this.authViewAuthorizationLabel = authViewAuthorizationLabel;
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

		public String getAuthCreateUserLabel()
		{
			return authCreateUserLabel;
		}

		public void setAuthCreateUserLabel(String authCreateUserLabel)
		{
			this.authCreateUserLabel = authCreateUserLabel;
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

		/**
		 * 查询、编辑操作是否必须首先设置资源。
		 * 
		 * @return
		 */
		public boolean mustAssignResource()
		{
			return (!this.supportSelectResource && !supportPatternResource);
		}

		public String buildLabelKey(String subKey)
		{
			return buildLabelKey(this.labelKeyPrefix, subKey);
		}

		public void updateResouceTypeLabel()
		{
			this.resouceTypeLabel = buildLabelKey("resouceTypeLabel");
		}

		public void updateAuthModeSelectResourceLabel()
		{
			this.authModeSelectResourceLabel = buildLabelKey("authModeSelectResourceLabel");
		}

		public void updateAuthModeSelectResourceLabelDesc()
		{
			this.authModeSelectResourceLabelDesc = buildLabelKey("authModeSelectResourceLabelDesc");
		}

		public void updateAuthModePatternResourceLabel()
		{
			this.authModePatternResourceLabel = buildLabelKey("authModePatternResourceLabel");
		}

		public void updateAuthModePatternResourceLabelDesc()
		{
			this.authModePatternResourceLabelDesc = buildLabelKey("authModePatternResourceLabelDesc");
		}

		public void updateAuthManageAuthorizationLabel()
		{
			this.authManageAuthorizationLabel = buildLabelKey("authManageAuthorizationLabel");
		}

		public void updateAuthAddAuthorizationLabel()
		{
			this.authAddAuthorizationLabel = buildLabelKey("authAddAuthorizationLabel");
		}

		public void updateAuthEditAuthorizationLabel()
		{
			this.authEditAuthorizationLabel = buildLabelKey("authEditAuthorizationLabel");
		}

		public void updateAuthViewAuthorizationLabel()
		{
			this.authViewAuthorizationLabel = buildLabelKey("authViewAuthorizationLabel");
		}

		public void updateAuthResourceLabel()
		{
			this.authResourceLabel = buildLabelKey("authResourceLabel");
		}

		public void updateAuthResourceTypeLabel()
		{
			this.authResourceTypeLabel = buildLabelKey("authResourceTypeLabel");
		}

		public void updateAuthPrincipalLabel()
		{
			this.authPrincipalLabel = buildLabelKey("authPrincipalLabel");
		}

		public void updateAuthPrincipalTypeLabel()
		{
			this.authPrincipalTypeLabel = buildLabelKey("authPrincipalTypeLabel");
		}

		public void updateAuthPermissionLabel()
		{
			this.authPermissionLabel = buildLabelKey("authPermissionLabel");
		}

		public void updateAuthEnabledLabel()
		{
			this.authEnabledLabel = buildLabelKey("authEnabledLabel");
		}

		public void updateAuthCreateUserLabel()
		{
			this.authCreateUserLabel = buildLabelKey("authCreateUserLabel");
		}

		public static String buildLabelKey(String labelKeyPrefix, String subKey)
		{
			return labelKeyPrefix + ".auth." + subKey;
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
			return new PermissionMeta(permission, "authorization.permission.READ");
		}

		public static PermissionMeta valueOfEdit()
		{
			return valueOfEdit(Authorization.PERMISSION_EDIT_START);
		}

		public static PermissionMeta valueOfEdit(int permission)
		{
			return new PermissionMeta(permission, "authorization.permission.EDIT");
		}

		public static PermissionMeta valueOfDelete()
		{
			return valueOfEdit(Authorization.PERMISSION_DELETE_START);
		}

		public static PermissionMeta valueOfDelete(int permission)
		{
			return new PermissionMeta(permission, "authorization.permission.DELETE");
		}

		public static PermissionMeta valueOfNone()
		{
			return valueOfEdit(Authorization.PERMISSION_NONE_START);
		}

		public static PermissionMeta valueOfNone(int permission)
		{
			return new PermissionMeta(permission, "authorization.permission.NONE");
		}

		public static PermissionMeta[] valuesOf(PermissionMeta... permissionMetas)
		{
			return permissionMetas;
		}
	}
}