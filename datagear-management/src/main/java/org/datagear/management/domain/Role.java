/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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

	/** 名称 */
	private String name;

	/** 描述 */
	private String description;

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
}
