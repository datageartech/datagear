/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.io.Serializable;

/**
 * 实体。
 * <p>
 * <i>实体</i>描述特定领域模型对象的实体数据结构。
 * </p>
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
