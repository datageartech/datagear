/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import org.springframework.beans.BeanUtils;

/**
 * 看板分享设置。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardShareSet extends AbstractStringIdEntity implements CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 是否启用分享密码 */
	private boolean enablePassword = false;

	/** 仅匿名用户需要输入分享密码 */
	private boolean anonymousPassword = false;

	/** 分享密码 */
	private String password = "";

	public DashboardShareSet()
	{
		super();
	}

	public DashboardShareSet(String id)
	{
		super(id);
	}

	public boolean isEnablePassword()
	{
		return enablePassword;
	}

	public void setEnablePassword(boolean enablePassword)
	{
		this.enablePassword = enablePassword;
	}

	public boolean isAnonymousPassword()
	{
		return anonymousPassword;
	}

	public void setAnonymousPassword(boolean anonymousPassword)
	{
		this.anonymousPassword = anonymousPassword;
	}

	/**
	 * 获取分享密码。
	 * <p>
	 * 返回{@code null}、{@code ""}表示分享密码为空。
	 * </p>
	 * 
	 * @return
	 */
	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public DashboardShareSet clone()
	{
		DashboardShareSet entity = new DashboardShareSet();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
