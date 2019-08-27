/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 数据ID权限。
 * 
 * @author datagear@163.com
 *
 */
public class DataIdPermission implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String dataId;

	private int permission;

	public DataIdPermission()
	{
		super();
	}

	public DataIdPermission(String dataId, int permission)
	{
		super();
		this.dataId = dataId;
		this.permission = permission;
	}

	public String getDataId()
	{
		return dataId;
	}

	public void setDataId(String dataId)
	{
		this.dataId = dataId;
	}

	public int getPermission()
	{
		return permission;
	}

	public void setPermission(int permission)
	{
		this.permission = permission;
	}
}
