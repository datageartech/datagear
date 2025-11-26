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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.datagear.management.domain.Role;

/**
 * 数据分析角色。
 * <p>
 * 这里不直接使用{@linkplain Role}，因为数据分析角色不应因{@linkplain Role}的改变而改变。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AnalysisRole implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** ID */
	private String id;

	/** 名称 */
	private String name;

	/** 是否启用 */
	private boolean enabled = true;

	public AnalysisRole(String id, String name, boolean enabled)
	{
		super();
		this.id = id;
		this.name = name;
		this.enabled = enabled;
	}

	public AnalysisRole(Role role)
	{
		this(role.getId(), role.getName(), role.isEnabled());
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

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
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
		AnalysisRole other = (AnalysisRole) obj;
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
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", enabled=" + enabled + "]";
	}

	/**
	 * 构建{@linkplain AnalysisRole}列表。
	 * 
	 * @param roles 允许为{@code null}
	 * @return 不会为{@code null}
	 */
	public static List<AnalysisRole> valueOf(Collection<Role> roles)
	{
		if (roles == null)
			return Collections.emptyList();

		List<AnalysisRole> analysisRoles = new ArrayList<AnalysisRole>(roles.size());

		for (Role role : roles)
			analysisRoles.add(new AnalysisRole(role));

		return analysisRoles;
	}
}