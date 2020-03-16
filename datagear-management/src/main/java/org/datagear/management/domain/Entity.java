/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 实体。
 * 
 * @author datagear@163.com
 *
 * @param <ID>
 */
public interface Entity<ID> extends Serializable
{
	/** ID属性名 */
	public static final String ID_PROP_NAME = "id";

	/**
	 * 获取ID。
	 * 
	 * @return
	 */
	ID getId();

	/**
	 * 设置ID。
	 * 
	 * @param id
	 */
	void setId(ID id);
}
