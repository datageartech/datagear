/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import org.datagear.model.support.Entity;

/**
 * 数据权限实体。
 * 
 * @author datagear@163.com
 *
 * @param <ID>
 */
public interface DataPermissionEntity<ID> extends Entity<ID>
{
	/** 权限未加载 */
	int PERMISSION_NOT_LOADED = -9;

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
