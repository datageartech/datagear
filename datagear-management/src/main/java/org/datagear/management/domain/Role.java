/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

/**
 * 角色。
 * 
 * @author datagear@163.com
 *
 */
public class Role extends AbstractStringIdEntity
{
	private static final long serialVersionUID = 1L;

	/**
	 * 内置角色前缀。
	 */
	public static final String BUILTIN_ROLE_PREFIX = "ROLE_";

	/**
	 * 角色ID：注册用户。 <br>
	 * 系统新添加和注册的用户都会关联此角色，便于用户针对注册用户授权。
	 */
	public static final String ROLE_REGISTRY = BUILTIN_ROLE_PREFIX + "REGISTRY";

	/**
	 * 角色ID：数据管理员。<br>
	 * 可以管理数据源、数据集、图表、看板。
	 */
	public static final String ROLE_DATA_ADMIN = BUILTIN_ROLE_PREFIX + "DATA_ADMIN";

	/**
	 * 角色ID：数据分析员。 <br>
	 * 仅可查看数据源、数据集、图表、看板，展示图表和看板。
	 */
	public static final String ROLE_DATA_ANALYST = BUILTIN_ROLE_PREFIX + "DATA_ANALYST";

	/** 名称 */
	private String name;

	/** 描述 */
	private String description = "";

	/** 是否启用 */
	private boolean enabled = true;

	public Role()
	{
		super();
	}

	public Role(String id, String name)
	{
		super(id);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * 给定角色ID是否是内置角色。
	 * 
	 * @param roleId
	 * @return
	 */
	public static boolean isBuiltinRole(String roleId)
	{
		return roleId != null && roleId.startsWith(BUILTIN_ROLE_PREFIX);
	}
}
