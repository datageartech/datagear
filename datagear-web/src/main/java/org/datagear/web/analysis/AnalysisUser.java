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

package org.datagear.web.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;
import org.datagear.management.domain.User;

/**
 * 数据分析用户。
 * <p>
 * 看板展示页面渲染上下文中的当前用户。
 * </p>
 * <p>
 * 这里不直接使用{@linkplain User}，因为数据分析用户不应因{@linkplain User}的改变而改变。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AnalysisUser implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 内置数据集参数：当前用户。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String DATA_SET_PARAM_NAME_CURRENT_USER = DataSetQuery.BUILTIN_PARAM_PREFIX + "USER";

	/**
	 * 内置数据集参数：当前角色名集。
	 * <p>
	 * 在数据集的参数化语境内，虽然可以通过{@code DG_USERS.ROLES}获取角色名集，但是语法较为繁琐，
	 * 考虑到角色名集可能使用较频繁，所以单独定义。
	 * </p>
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES = DataSetQuery.BUILTIN_PARAM_PREFIX
			+ "ROLE_NAMES";

	/** ID */
	private String id;

	/** 用户名 */
	private String name;

	/** 姓名 */
	private String realName;

	/** 是否管理员 */
	private boolean admin = false;

	/** 是否是匿名用户 */
	private boolean anonymous = false;

	/** 角色集 */
	private List<AnalysisRole> roles = Collections.emptyList();

	public AnalysisUser(String id, String name, String realName, boolean admin, boolean anonymous,
			List<AnalysisRole> roles)
	{
		super();
		this.id = id;
		this.name = name;
		this.realName = realName;
		this.admin = admin;
		this.anonymous = anonymous;
		this.roles = roles;
	}

	public AnalysisUser(User user)
	{
		this(user.getId(), user.getName(), user.getRealName(), user.isAdmin(), user.isAnonymous(),
				AnalysisRole.valueOf(user.getRoles()));
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}

	public boolean isAnonymous()
	{
		return anonymous;
	}

	public void setAnonymous(boolean anonymous)
	{
		this.anonymous = anonymous;
	}

	public List<AnalysisRole> getRoles()
	{
		return roles;
	}

	public void setRoles(List<AnalysisRole> roles)
	{
		this.roles = roles;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisUser other = (AnalysisUser) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", realName=" + realName + ", admin="
				+ admin + ", anonymous=" + anonymous + ", roles=" + roles + "]";
	}

	/**
	 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
	 * {@linkplain #getEnabledRoleNames(AnalysisUser)}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
	 * 名加入{@linkplain DataSetQuery#getParamValues()}。
	 * <p>
	 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
	 * </p>
	 * 
	 * @param dataSetQuery
	 */
	public void setParamValue(DataSetQuery dataSetQuery)
	{
		setParamValue(dataSetQuery, getEnabledRoleNames());
	}

	/**
	 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
	 * {@code analysisRoleNames}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
	 * 名加入{@linkplain DataSetQuery#getParamValues()}。
	 * <p>
	 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
	 * </p>
	 * 
	 * @param dataSetQuery
	 * @param analysisRoleNames
	 */
	public void setParamValue(DataSetQuery dataSetQuery, List<String> analysisRoleNames)
	{
		if (analysisRoleNames == null)
			throw new IllegalArgumentException("[analysisRoleNames] required");

		dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_USER, this);
		dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES, analysisRoleNames);
	}

	/**
	 * 获取{@linkplain AnalysisUser#getRoles()}列表中已启用的{@linkplain AnalysisRole#getName()}列表。
	 * 
	 * @return 不会为{@code null}
	 */
	public List<String> getEnabledRoleNames()
	{
		List<String> roleNames = new ArrayList<String>();

		if (this.roles != null)
		{
			for (AnalysisRole role : this.roles)
			{
				if (role.isEnabled())
					roleNames.add(role.getName());
			}
		}

		return roleNames;
	}
}