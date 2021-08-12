/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 数据ID权限。
 * 
 * @author datagear@163.com
 *
 */
public class DataIdPermission implements DataPermissionAware, Serializable
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
		this.dataPermission = Authorization.trimPermission(dataPermission);
	}
}
