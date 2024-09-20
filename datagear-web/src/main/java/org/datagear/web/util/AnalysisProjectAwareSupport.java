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

package org.datagear.web.util;

import java.util.function.Function;
import java.util.function.Supplier;

import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.util.ManagementSupport;
import org.datagear.management.util.RefPermissionDeniedException;

/**
 * {@linkplain AnalysisProjectAwareEntity}支持类。
 * 
 * @author datagear@163.com
 *
 */
public class AnalysisProjectAwareSupport
{
	private ManagementSupport managementSupport;

	public AnalysisProjectAwareSupport()
	{
		super();
	}

	public AnalysisProjectAwareSupport(ManagementSupport managementSupport)
	{
		super();
		this.managementSupport = managementSupport;
	}

	public ManagementSupport getManagementSupport()
	{
		return managementSupport;
	}

	public void setManagementSupport(ManagementSupport managementSupport)
	{
		this.managementSupport = managementSupport;
	}

	/**
	 * 整理{@linkplain AnalysisProjectAwareEntity}：
	 * 如果{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}为{@code null}或其ID为空，
	 * 则将其改为{@code null}。
	 * 
	 * @param entity
	 */
	public void trim(AnalysisProjectAwareEntity entity)
	{
		this.managementSupport.trimRef(entity, (t) ->
		{
			return t.getAnalysisProject();

		}, (t) ->
		{
			t.setAnalysisProject(null);
		});
	}

	/**
	 * 如果用户对{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}没有权限，则将其置为{@code null}。
	 * 
	 * @param user
	 * @param entity
	 * @param service
	 */
	public void setRefNullIfDenied(User user, AnalysisProjectAwareEntity entity,
			AnalysisProjectService service)
	{
		this.managementSupport.setRefNullIfDenied(user, entity, (t) ->
		{
			return t.getAnalysisProject();

		}, (t) ->
		{
			t.setAnalysisProject(null);

		}, service);
	}

	/**
	 * 校验{@linkplain AnalysisProjectAwareEntity}保存操作的{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}是否越权。
	 * <p>
	 * 另参考{@linkplain ManagementSupport#checkSaveRefSupplier(User, Object, Supplier, Function, Function, DataPermissionEntityService)}
	 * </p>
	 * 
	 * @param user
	 * @param entity
	 * @param persist
	 * @param service
	 * @throws RefPermissionDeniedException
	 */
	public void checkSaveSupplier(User user, AnalysisProjectAwareEntity entity,
			Supplier<AnalysisProjectAwareEntity> persist, AnalysisProjectService service)
			throws RefPermissionDeniedException
	{
		this.managementSupport.checkSaveRefSupplier(user, entity, persist, (t) ->
		{
			return t.getAnalysisProject();

		}, (r) ->
		{
			return r.getName();

		}, service);
	}
}
