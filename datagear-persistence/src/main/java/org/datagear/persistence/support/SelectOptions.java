/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.Serializable;

/**
 * 查询持久化操作的配置选项。
 * 
 * @author datagear@163.com
 *
 */
public class SelectOptions implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 关联查询最大嵌套级 */
	private int maxQueryDepth = 2;

	public SelectOptions()
	{
		super();
	}

	public int getMaxQueryDepth()
	{
		return maxQueryDepth;
	}

	public void setMaxQueryDepth(int maxQueryDepth)
	{
		this.maxQueryDepth = maxQueryDepth;
	}
}
