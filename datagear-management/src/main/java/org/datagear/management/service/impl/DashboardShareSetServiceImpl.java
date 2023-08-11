/*
 * Copyright 2018-2023 datagear.tech
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

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.DashboardShareSet;
import org.datagear.management.service.DashboardShareSetService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * {@linkplain DashboardShareSetService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardShareSetServiceImpl extends AbstractMybatisEntityService<String, DashboardShareSet>
		implements DashboardShareSetService
{
	protected static final String SQL_NAMESPACE = DashboardShareSet.class.getName();

	private TextEncryptor textEncryptor;

	public DashboardShareSetServiceImpl()
	{
		super();
	}

	public DashboardShareSetServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			TextEncryptor textEncryptor)
	{
		super(sqlSessionFactory, dialect);
		this.textEncryptor = textEncryptor;
	}

	public DashboardShareSetServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			TextEncryptor textEncryptor)
	{
		super(sqlSessionTemplate, dialect);
		this.textEncryptor = textEncryptor;
	}

	public TextEncryptor getTextEncryptor()
	{
		return textEncryptor;
	}

	public void setTextEncryptor(TextEncryptor textEncryptor)
	{
		this.textEncryptor = textEncryptor;
	}

	@Override
	public void save(DashboardShareSet entity)
	{
		if (!super.update(entity))
			super.add(entity);
	}

	@Override
	protected void add(DashboardShareSet entity, Map<String, Object> params)
	{
		entity = entity.clone();
		entity.setPassword(this.textEncryptor.encrypt(entity.getPassword()));

		super.add(entity, params);
	}

	@Override
	protected boolean update(DashboardShareSet entity, Map<String, Object> params)
	{
		entity = entity.clone();
		entity.setPassword(this.textEncryptor.encrypt(entity.getPassword()));

		return super.update(entity, params);
	}

	@Override
	protected DashboardShareSet getByIdFromDB(String id, Map<String, Object> params)
	{
		DashboardShareSet entity = super.getByIdFromDB(id, params);

		if (entity != null)
			entity.setPassword(this.textEncryptor.decrypt(entity.getPassword()));

		return entity;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
