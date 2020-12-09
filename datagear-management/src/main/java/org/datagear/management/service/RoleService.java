/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import org.datagear.management.domain.Role;

/**
 * {@linkplain Role}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface RoleService extends EntityService<String, Role>
{
	@Override
	boolean deleteById(String id) throws DeleteBuiltinRoleDeniedException;

	@Override
	boolean[] deleteByIds(String[] ids) throws DeleteBuiltinRoleDeniedException;
}
