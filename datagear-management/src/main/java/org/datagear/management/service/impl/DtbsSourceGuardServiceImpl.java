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

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.DtbsSourceGuard;
import org.datagear.management.domain.User;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.util.DtbsSourceGuardChecker;
import org.datagear.management.util.GuardEntity;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.Query;
import org.datagear.util.LastModifiedService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain DtbsSourceGuardService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceGuardServiceImpl extends AbstractMybatisEntityService<String, DtbsSourceGuard>
		implements DtbsSourceGuardService
{
	protected static final String SQL_NAMESPACE = DtbsSourceGuard.class.getName();

	protected static final String LAST_MODIFIED_NAME = DtbsSourceGuardService.class.getName();

	private DtbsSourceGuardChecker dtbsSourceGuardChecker = new DtbsSourceGuardChecker();

	private LastModifiedService lastModifiedService;

	private volatile List<DtbsSourceGuard> _dtbsSourceGuards = Collections.emptyList();
	private volatile long _dtbsSourceGuardsLastModified = LastModifiedService.LAST_MODIFIED_INIT;

	public DtbsSourceGuardServiceImpl()
	{
		super();
	}

	public DtbsSourceGuardServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			LastModifiedService lastModifiedService)
	{
		super(sqlSessionFactory, dialect);
		this.lastModifiedService = lastModifiedService;
	}

	public DtbsSourceGuardServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			LastModifiedService lastModifiedService)
	{
		super(sqlSessionTemplate, dialect);
		this.lastModifiedService = lastModifiedService;
	}

	public DtbsSourceGuardChecker getDtbsSourceGuardChecker()
	{
		return dtbsSourceGuardChecker;
	}

	public void setDtbsSourceGuardChecker(DtbsSourceGuardChecker dtbsSourceGuardChecker)
	{
		this.dtbsSourceGuardChecker = dtbsSourceGuardChecker;
	}

	public LastModifiedService getLastModifiedService()
	{
		return lastModifiedService;
	}

	public void setLastModifiedService(LastModifiedService lastModifiedService)
	{
		this.lastModifiedService = lastModifiedService;
	}

	@Override
	public boolean isPermitted(GuardEntity guardEntity)
	{
		if (this.lastModifiedService.isModified(LAST_MODIFIED_NAME, this._dtbsSourceGuardsLastModified))
		{
			this._dtbsSourceGuardsLastModified = this.lastModifiedService.getLastModified(LAST_MODIFIED_NAME);

			List<DtbsSourceGuard> dtbsSourceGuards = query("getAll", new Query(), buildParamMap(), true);
			DtbsSourceGuard.sortByPriority(dtbsSourceGuards);
			this._dtbsSourceGuards = Collections.unmodifiableList(new ArrayList<DtbsSourceGuard>(dtbsSourceGuards));
		}

		return this.dtbsSourceGuardChecker.isPermitted(this._dtbsSourceGuards, guardEntity);
	}

	@Override
	public boolean isPermitted(User user, GuardEntity guardEntity)
	{
		if (user.isAdmin())
			return true;

		return this.isPermitted(guardEntity);
	}

	@Override
	protected boolean update(DtbsSourceGuard entity, Map<String, Object> params)
	{
		boolean re = super.update(entity, params);
		updateDtbsSourceGuardsLastModified();

		return re;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean re = super.deleteById(id, params);
		updateDtbsSourceGuardsLastModified();

		return re;
	}

	@Override
	protected void add(DtbsSourceGuard entity, Map<String, Object> params)
	{
		super.add(entity, params);
		updateDtbsSourceGuardsLastModified();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}

	protected void updateDtbsSourceGuardsLastModified()
	{
		this.lastModifiedService.setLastModifiedNow(LAST_MODIFIED_NAME);
	}
}
