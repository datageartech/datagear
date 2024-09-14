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

package org.datagear.management.util;

import org.datagear.management.service.PermissionDeniedException;

/**
 * 引用对象无权访问异常。
 * 
 * @author datagear@163.com
 *
 */
public class RefPermissionDeniedException extends PermissionDeniedException
{
	private static final long serialVersionUID = 1L;

	/** 引用对象名 */
	private String refName;

	public RefPermissionDeniedException()
	{
		super();
	}

	public RefPermissionDeniedException(String refName)
	{
		super("Permission denied for : " + refName);
	}

	public RefPermissionDeniedException(String refName, Throwable cause)
	{
		super("Permission denied for : " + refName, cause);
	}

	public String getRefName()
	{
		return refName;
	}

	public void setRefName(String refName)
	{
		this.refName = refName;
	}
}
