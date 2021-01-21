/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

/**
 * 抽象字符串ID实体。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractStringIdEntity extends AbstractEntity<String>
{
	private static final long serialVersionUID = 1L;

	public AbstractStringIdEntity()
	{
		super();
	}

	public AbstractStringIdEntity(String id)
	{
		super(id);
	}
}
