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

import java.io.Serializable;

import org.springframework.beans.BeanUtils;

/**
 * 数据ID权限。
 * 
 * @author datagear@163.com
 *
 */
public class DataIdPermission implements DataPermissionEntity, Serializable, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	private String dataId;

	private int dataPermission;

	public DataIdPermission()
	{
		super();
	}

	public DataIdPermission(String dataId, int dataPermission)
	{
		super();
		this.dataId = dataId;
		this.dataPermission = dataPermission;
	}

	public String getDataId()
	{
		return dataId;
	}

	public void setDataId(String dataId)
	{
		this.dataId = dataId;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}

	@Override
	public DataIdPermission clone()
	{
		DataIdPermission entity = new DataIdPermission();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
