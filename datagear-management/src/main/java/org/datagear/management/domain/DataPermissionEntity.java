/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.domain;

import org.datagear.management.service.DataPermissionEntityService;

/**
 * 数据权限实体。
 * 
 * @author datagear@163.com
 *
 */
public interface DataPermissionEntity
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
