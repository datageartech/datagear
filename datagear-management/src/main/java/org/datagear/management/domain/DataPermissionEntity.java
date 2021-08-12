/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import org.datagear.management.service.DataPermissionEntityService;

/**
 * 数据权限实体。
 * 
 * @author datagear@163.com
 *
 * @param <ID>
 */
public interface DataPermissionEntity<ID> extends DataPermissionAware, Entity<ID>
{
	/** 参考{@linkplain DataPermissionEntityService#PERMISSION_NOT_LOADED} */
	int PERMISSION_NOT_LOADED = DataPermissionEntityService.PERMISSION_NOT_LOADED;
}
