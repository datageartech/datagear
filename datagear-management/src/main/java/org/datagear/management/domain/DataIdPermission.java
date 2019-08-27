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

	public int getDataPermission()
	{
		return dataPermission;
	}

	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}
}
