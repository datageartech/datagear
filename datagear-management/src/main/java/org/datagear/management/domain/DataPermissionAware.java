/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

/**
 * 数据权限相关模型。
 * 
 * @author datagear@163.com
 *
 */
public interface DataPermissionAware
{
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
	 * <p>
	 * 底层SQL查询会对数据库中存储的权限值进行数学运算，
	 * 可能导致查询的权限值大于{@linkplain Authorization#PERMISSION_MAX}且个位和十位数为实际权限值，
	 * 为了提高系统的数据库兼容性，所以尽量不使用数据库特性（比如{@code MOD}函数）
	 * 因此，对于这个方法，实现类应该对参数值进行{@code permission%100}取余处理（可使用{@linkplain Authorization#trimPermission(int)}），以确保权限值正确。
	 * </p>
	 * 
	 * @param permission
	 */
	void setDataPermission(int permission);
}
