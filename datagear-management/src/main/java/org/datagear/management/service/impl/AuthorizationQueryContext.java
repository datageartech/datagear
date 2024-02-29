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

package org.datagear.management.service.impl;

import java.io.Serializable;

import org.datagear.management.domain.Authorization;
import org.datagear.management.service.ServiceContext;
import org.datagear.util.StringUtil;

/**
 * {@linkplain Authorization}查询上下文。
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationQueryContext implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String NAME_FOR_SERVICE_CONTEXT = AuthorizationQueryContext.class.getSimpleName();

	/** 结果集中的全部用户标签 */
	private String principalAllLabel = Authorization.PRINCIPAL_ALL;

	/** 结果集中的全部匿名用户标签 */
	private String principalAnonymousLabel = Authorization.PRINCIPAL_ANONYMOUS;

	/** 权限值标签 */
	private EnumValueLabel<Integer>[] permissionLabels;

	/** 指定查询资源类型 */
	private String resourceType = null;

	/** 指定查询资源 */
	private String resource = null;

	public AuthorizationQueryContext()
	{
		super();
	}

	public String getPrincipalAllLabel()
	{
		return principalAllLabel;
	}

	public void setPrincipalAllLabel(String principalAllLabel)
	{
		this.principalAllLabel = principalAllLabel;
	}

	public String getPrincipalAnonymousLabel()
	{
		return principalAnonymousLabel;
	}

	public void setPrincipalAnonymousLabel(String principalAnonymousLabel)
	{
		this.principalAnonymousLabel = principalAnonymousLabel;
	}

	public boolean hasPermissionLabels()
	{
		return (this.permissionLabels != null && this.permissionLabels.length > 0);
	}

	public EnumValueLabel<Integer>[] getPermissionLabels()
	{
		return permissionLabels;
	}

	public void setPermissionLabels(EnumValueLabel<Integer>[] permissionLabels)
	{
		this.permissionLabels = permissionLabels;
	}

	public boolean hasResourceType()
	{
		return !StringUtil.isEmpty(this.resourceType);
	}

	public String getResourceType()
	{
		return resourceType;
	}

	public void setResourceType(String resourceType)
	{
		this.resourceType = resourceType;
	}

	public boolean hasResource()
	{
		return !StringUtil.isEmpty(this.resource);
	}

	public String getResource()
	{
		return resource;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	/**
	 * 将{@linkplain AuthorizationQueryContext}存入{@linkplain ServiceContext}。
	 * 
	 * @param context
	 */
	public static void set(AuthorizationQueryContext context)
	{
		ServiceContext.get().setValue(NAME_FOR_SERVICE_CONTEXT, context);
	}

	/**
	 * 从{@linkplain ServiceContext}取得{@linkplain AuthorizationQueryContext}。
	 * <p>
	 * 如果先前未存入，此方法将返回默认对象。
	 * </p>
	 * 
	 * @return
	 */
	public static AuthorizationQueryContext get()
	{
		AuthorizationQueryContext context = ServiceContext.get().getValue(NAME_FOR_SERVICE_CONTEXT);

		if (context == null)
			context = new AuthorizationQueryContext();

		return context;
	}
}
