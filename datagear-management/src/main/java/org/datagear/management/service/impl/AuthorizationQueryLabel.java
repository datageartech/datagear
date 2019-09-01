/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.io.Serializable;

import org.datagear.management.domain.Authorization;

/**
 * {@linkplain Authorization}查询标签。
 * <p>
 * 此类用于设置查询结果枚举值的展示标签。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationQueryLabel implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String CUSTOM_QUERY_PARAMETER_NAME = "authorizationQueryLabel";

	private String principalAll = Authorization.PRINCIPAL_ALL;

	private String principalAnonymous = Authorization.PRINCIPAL_ANONYMOUS;

	public AuthorizationQueryLabel()
	{
		super();
	}

	public String getPrincipalAll()
	{
		return principalAll;
	}

	public void setPrincipalAll(String principalAll)
	{
		this.principalAll = principalAll;
	}

	public String getPrincipalAnonymous()
	{
		return principalAnonymous;
	}

	public void setPrincipalAnonymous(String principalAnonymous)
	{
		this.principalAnonymous = principalAnonymous;
	}
}
