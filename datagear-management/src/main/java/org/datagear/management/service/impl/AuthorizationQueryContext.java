/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.io.Serializable;

import org.datagear.management.domain.Authorization;
import org.datagear.management.service.ServiceContext;

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
		return (this.resourceType != null && !this.resourceType.isEmpty());
	}

	public String getResourceType()
	{
		return resourceType;
	}

	public void setResourceType(String resourceType)
	{
		this.resourceType = resourceType;
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
