/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
public interface DataPermissionEntity<ID> extends Entity<ID>
{
	/** 参考{@linkplain DataPermissionEntityService#PERMISSION_NOT_LOADED} */
	int PERMISSION_NOT_LOADED = DataPermissionEntityService.PERMISSION_NOT_LOADED;

	/**
	 * 获取数据权限值。
	 * <p>
	 * 参考{@code Authorization.PERMISSION_*}、{@linkplain #PERMISSION_NOT_LOADED}。
	 * </p>
	 * 
	 * @return
	 */
	int getDataPermission();

	/**
	 * 设置数据权限值。
	 * <p>
	 * 参考{@code Authorization.PERMISSION_*}、{@linkplain #PERMISSION_NOT_LOADED}。
	 * </p>
	 * 
	 * @param permission
	 */
	void setDataPermission(int permission);
}
