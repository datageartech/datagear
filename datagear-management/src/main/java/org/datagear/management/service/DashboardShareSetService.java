/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.DashboardShareSet;

/**
 * {@linkplain DashboardShareSet}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DashboardShareSetService extends EntityService<String, DashboardShareSet>
{
	/**
	 * 保存。
	 * <p>
	 * 插入或更新。
	 * </p>
	 * 
	 * @param entity
	 */
	void save(DashboardShareSet entity);
}
