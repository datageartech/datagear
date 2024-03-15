/*
 * Copyright 2018-present datagear.tech
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
