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
import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaGuardService;
import org.datagear.management.util.GuardEntity;
import org.datagear.management.util.SchemaGuardChecker;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.Query;
import org.datagear.util.LastModifiedService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SchemaGuardService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuardServiceImpl extends AbstractMybatisEntityService<String, SchemaGuard>
		implements SchemaGuardService
{
	protected static final String SQL_NAMESPACE = SchemaGuard.class.getName();

	protected static final String LAST_MODIFIED_NAME = SchemaGuardService.class.getName();

	private SchemaGuardChecker schemaGuardChecker = new SchemaGuardChecker();

	private LastModifiedService lastModifiedService;

	private volatile List<SchemaGuard> _schemaGuards = Collections.emptyList();
	private volatile long _schemaGuardsLastModified = LastModifiedService.LAST_MODIFIED_INIT;

	public SchemaGuardServiceImpl()
	{
		super();
	}

	public SchemaGuardServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			LastModifiedService lastModifiedService)
	{
		super(sqlSessionFactory, dialect);
		this.lastModifiedService = lastModifiedService;
	}

	public SchemaGuardServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			LastModifiedService lastModifiedService)
	{
		super(sqlSessionTemplate, dialect);
		this.lastModifiedService = lastModifiedService;
	}

	public SchemaGuardChecker getSchemaGuardChecker()
	{
		return schemaGuardChecker;
	}

	public void setSchemaGuardChecker(SchemaGuardChecker schemaGuardChecker)
	{
		this.schemaGuardChecker = schemaGuardChecker;
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
		if (this.lastModifiedService.isModified(LAST_MODIFIED_NAME, this._schemaGuardsLastModified))
		{
			this._schemaGuardsLastModified = this.lastModifiedService.getLastModified(LAST_MODIFIED_NAME);

			List<SchemaGuard> schemaGuards = query("getAll", new Query(), buildParamMap(), true);
			SchemaGuard.sortByPriority(schemaGuards);
			this._schemaGuards = Collections.unmodifiableList(new ArrayList<SchemaGuard>(schemaGuards));
		}

		return this.schemaGuardChecker.isPermitted(this._schemaGuards, guardEntity);
	}

	@Override
	public boolean isPermitted(User user, GuardEntity guardEntity)
	{
		if (user.isAdmin())
			return true;

		return this.isPermitted(guardEntity);
	}

	@Override
	protected boolean update(SchemaGuard entity, Map<String, Object> params)
	{
		boolean re = super.update(entity, params);
		updateSchemaGuardsLastModified();

		return re;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean re = super.deleteById(id, params);
		updateSchemaGuardsLastModified();

		return re;
	}

	@Override
	protected void add(SchemaGuard entity, Map<String, Object> params)
	{
		super.add(entity, params);
		updateSchemaGuardsLastModified();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}

	protected void updateSchemaGuardsLastModified()
	{
		this.lastModifiedService.setLastModifiedNow(LAST_MODIFIED_NAME);
	}
}
