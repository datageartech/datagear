/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import org.datagear.model.support.AbstractStringIdEntity;

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

	/** 是否已禁用 */
	private boolean disabled = false;

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

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
}
