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
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationResourceMetas
{
	public static final String LABEL_KEY_PREFIX = "authorization.resourceMeta.";

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
			ResourceMeta resourceMeta = ResourceMeta.valueOf(Schema.AUTHORIZATION_RESOURCE_TYPE, "/schema/select", "id",
					"title", true,
					PermissionMeta.valuesOf(Schema.AUTHORIZATION_RESOURCE_TYPE, Schema.PERMISSION_TABLE_DATA_READ,
							Schema.PERMISSION_TABLE_DATA_EDIT, Schema.PERMISSION_TABLE_DATA_DELETE,
							Authorization.PERMISSION_NONE_START, true));

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

		private String resourceType;

		private String resouceTypeLabelKey;

		private String selectResourceURL;

		private String selectResourceIdField;

		private String selectResourceNameField;

		private PermissionMeta[] permissionMetas;

		private boolean supportPattern = false;

		private String selectResourceLabelKey;

		private String selectResourceLabelDescKey;

		private String fillPatternLabelKey;

		private String fillPatternLabelDescKey;

		public ResourceMeta()
		{
			super();
		}

		public ResourceMeta(String resourceType, String resouceTypeLabelKey, String selectResourceURL,
				String selectResourceIdField, String selectResourceNameField, PermissionMeta... permissionMetas)
		{
			super();
			this.resourceType = resourceType;
			this.resouceTypeLabelKey = resouceTypeLabelKey;
			this.selectResourceURL = selectResourceURL;
			this.selectResourceIdField = selectResourceIdField;
			this.selectResourceNameField = selectResourceNameField;
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

		public String getResouceTypeLabelKey()
		{
			return resouceTypeLabelKey;
		}

		public void setResouceTypeLabelKey(String resouceTypeLabelKey)
		{
			this.resouceTypeLabelKey = resouceTypeLabelKey;
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

		public PermissionMeta[] getPermissionMetas()
		{
			return permissionMetas;
		}

		public void setPermissionMetas(PermissionMeta... permissionMetas)
		{
			this.permissionMetas = permissionMetas;
		}

		public boolean isSupportPattern()
		{
			return supportPattern;
		}

		public void setSupportPattern(boolean supportPattern)
		{
			this.supportPattern = supportPattern;
		}

		public String getSelectResourceLabelKey()
		{
			return selectResourceLabelKey;
		}

		public void setSelectResourceLabelKey(String selectResourceLabelKey)
		{
			this.selectResourceLabelKey = selectResourceLabelKey;
		}

		public String getSelectResourceLabelDescKey()
		{
			return selectResourceLabelDescKey;
		}

		public void setSelectResourceLabelDescKey(String selectResourceLabelDescKey)
		{
			this.selectResourceLabelDescKey = selectResourceLabelDescKey;
		}

		public String getFillPatternLabelKey()
		{
			return fillPatternLabelKey;
		}

		public void setFillPatternLabelKey(String fillPatternLabelKey)
		{
			this.fillPatternLabelKey = fillPatternLabelKey;
		}

		public String getFillPatternLabelDescKey()
		{
			return fillPatternLabelDescKey;
		}

		public void setFillPatternLabelDescKey(String fillPatternLabelDescKey)
		{
			this.fillPatternLabelDescKey = fillPatternLabelDescKey;
		}

		public static ResourceMeta valueOf(String resourceType, String selectResourceURL, String selectResourceIdField,
				String selectResourceNameField, boolean supportPattern, PermissionMeta... permissionMetas)
		{
			String resouceTypeLabelKey = LABEL_KEY_PREFIX + resourceType + ".resouceTypeLabel";
			ResourceMeta meta = new ResourceMeta(resourceType, resouceTypeLabelKey, selectResourceURL,
					selectResourceIdField, selectResourceNameField, permissionMetas);

			if (supportPattern)
			{
				String selectResourceLabelKey = LABEL_KEY_PREFIX + resourceType + ".selectResourceLabel";
				String selectResourceLabelDescKey = selectResourceLabelKey + ".desc";
				String fillPatternLabelKey = LABEL_KEY_PREFIX + resourceType + ".fillPatternLabel";
				String fillPatternLabelDescKey = fillPatternLabelKey + ".desc";

				meta.setSupportPattern(true);
				meta.setSelectResourceLabelKey(selectResourceLabelKey);
				meta.setSelectResourceLabelDescKey(selectResourceLabelDescKey);
				meta.setFillPatternLabelKey(fillPatternLabelKey);
				meta.setFillPatternLabelDescKey(fillPatternLabelDescKey);
			}

			return meta;
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

		private int permission;

		private String permissionLabelKey;

		private String permissionLabelDescKey;

		public PermissionMeta()
		{
			super();
		}

		public PermissionMeta(int permission, String permissionLabelKey, String permissionLabelDescKey)
		{
			super();
			this.permission = permission;
			this.permissionLabelKey = permissionLabelKey;
			this.permissionLabelDescKey = permissionLabelDescKey;
		}

		public int getPermission()
		{
			return permission;
		}

		public void setPermission(int permission)
		{
			this.permission = permission;
		}

		public String getPermissionLabelKey()
		{
			return permissionLabelKey;
		}

		public void setPermissionLabelKey(String permissionLabelKey)
		{
			this.permissionLabelKey = permissionLabelKey;
		}

		public String getPermissionLabelDescKey()
		{
			return permissionLabelDescKey;
		}

		public void setPermissionLabelDescKey(String permissionLabelDescKey)
		{
			this.permissionLabelDescKey = permissionLabelDescKey;
		}

		public static PermissionMeta valueOf(int permission, String permissionLabelKey)
		{
			return new PermissionMeta(permission, permissionLabelKey, permissionLabelKey + ".desc");
		}

		public static PermissionMeta[] valuesOf()
		{
			PermissionMeta[] permissionMetas = new PermissionMeta[4];

			permissionMetas[0] = valueOf(Authorization.PERMISSION_READ_START, "authorization.permission.READ");
			permissionMetas[1] = valueOf(Authorization.PERMISSION_EDIT_START, "authorization.permission.EDIT");
			permissionMetas[2] = valueOf(Authorization.PERMISSION_DELETE_START, "authorization.permission.DELETE");
			permissionMetas[3] = valueOf(Authorization.PERMISSION_NONE_START, "authorization.permission.NONE");

			return permissionMetas;
		}

		public static PermissionMeta[] valuesOf(String resourceType, int read, int edit, int delete, int none,
				boolean customDesc)
		{
			PermissionMeta[] permissionMetas = new PermissionMeta[4];

			permissionMetas[0] = valueOf(read, "authorization.permission.READ");
			permissionMetas[1] = valueOf(edit, "authorization.permission.EDIT");
			permissionMetas[2] = valueOf(delete, "authorization.permission.DELETE");
			permissionMetas[3] = valueOf(none, "authorization.permission.NONE");

			if (customDesc)
			{
				permissionMetas[0].setPermissionLabelDescKey(LABEL_KEY_PREFIX + resourceType + ".permission.READ.desc");
				permissionMetas[1].setPermissionLabelDescKey(LABEL_KEY_PREFIX + resourceType + ".permission.EDIT.desc");
				permissionMetas[2]
						.setPermissionLabelDescKey(LABEL_KEY_PREFIX + resourceType + ".permission.DELETE.desc");
				permissionMetas[3].setPermissionLabelDescKey(LABEL_KEY_PREFIX + resourceType + ".permission.NONE.desc");
			}

			return permissionMetas;
		}
	}
}