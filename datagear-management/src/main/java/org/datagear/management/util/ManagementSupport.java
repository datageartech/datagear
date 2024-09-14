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

package org.datagear.management.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.domain.User;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.util.StringUtil;

/**
 * 管理操作支持类。
 * 
 * @author datagear@163.com
 *
 */
public class ManagementSupport
{
	public ManagementSupport()
	{
		super();
	}

	/**
	 * 整理关联引用实体，如果关联引用实体为{@code null}或其ID为空，则将其设置为{@code null}。
	 * <p>
	 * 某些数据库，外键不允许空字符串，需要置为{@code null}。
	 * </p>
	 * 
	 * @param <T>
	 * @param <R>
	 * @param entity
	 * @param getter
	 * @param nullSetter
	 */
	public <T, R extends Entity<?>> void trimRef(T entity, Function<T, R> getter, Consumer<T> nullSetter)
	{
		if (entity == null)
			return;

		R ref = getter.apply(entity);

		if (ref == null)
			return;

		if (StringUtil.isEmpty(ref.getId()))
		{
			nullSetter.accept(entity);
		}
	}

	/**
	 * 如果用户对关联引用实体没有权限，则设置为{@code null}。
	 * 
	 * @param <T>
	 * @param <RID>
	 * @param <R>
	 * @param user
	 * @param entity
	 * @param getter
	 * @param nullSetter
	 * @param service
	 */
	public <T, RID, R extends DataPermissionEntity & Entity<RID>> void setRefNullIfNoPermission(
			User user, T entity, Function<T, R> getter, Consumer<T> nullSetter,
			DataPermissionEntityService<RID, R> service)
	{
		if (entity == null)
			return;

		R ref = getter.apply(entity);

		if (ref == null)
			return;

		int permission = service.getPermission(user, ref.getId());

		// 没有读权限，则置为null
		if (!Authorization.canRead(permission))
			nullSetter.accept(entity);
	}

	/**
	 * 校验添加保存/编辑保存操作对关联引用实体是否越权。
	 * 
	 * @param <T>
	 * @param <RID>
	 * @param <R>
	 * @param user
	 * @param entity
	 * @param persist
	 *            允许{@code null}，表示执行添加保存操作；，否则，表示执行编辑保存操作
	 * @param getter
	 * @param nameGetter
	 * @param service
	 * @throws RefPermissionDeniedException
	 */
	public <T, RID, R extends DataPermissionEntity & Entity<RID>> void checkSaveRefPermission(User user,
			T entity, T persist, Function<T, R> getter, Function<R, String> nameGetter,
			DataPermissionEntityService<RID, R> service) throws RefPermissionDeniedException
	{
		if (entity == null)
			return;

		R ref = getter.apply(entity);

		if (ref == null)
			return;

		RID rid = ref.getId();

		if (rid == null)
			return;

		// 对于编辑操作，只要没有修改，不应校验
		// 比如用户A编辑保存B分享的实体，应该保留B之前设置的有权限的关联实体C，即使A对C没有权限
		if (persist != null)
		{
			R eref = getter.apply(persist);
			RID erid = (eref == null ? null : eref.getId());

			if (StringUtil.isEquals(rid, erid))
				return;
		}

		// 添加、编辑且有修改

		R pref = service.getById(rid);

		// 忽略，不在这里处理
		if (pref == null)
			return;

		int permission = service.getPermission(user, rid);

		if (!Authorization.canRead(permission))
		{
			String refName = nameGetter.apply(pref);
			throw new RefPermissionDeniedException(refName);
		}
	}
}
