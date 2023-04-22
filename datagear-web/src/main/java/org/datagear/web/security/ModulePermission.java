/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.web.security;

import java.io.Serializable;

/**
 * 模块权限。
 * 
 * @author datagear@163.com
 *
 */
public class ModulePermission implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 访问 */
	private boolean accessor = false;

	/** 操作 */
	private boolean operator = false;

	/** 可见 */
	private boolean visible = false;

	public ModulePermission()
	{
		super();
	}

	public ModulePermission(boolean accessor, boolean operator, boolean visible)
	{
		super();
		this.accessor = accessor;
		this.operator = operator;
		this.visible = visible;
	}

	public boolean isAccessor()
	{
		return accessor;
	}

	public void setAccessor(boolean accessor)
	{
		this.accessor = accessor;
	}

	public boolean isOperator()
	{
		return operator;
	}

	public void setOperator(boolean operator)
	{
		this.operator = operator;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
