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

package org.datagear.management.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.dbversion.DbVersionManager;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.management.util.dialect.MbSqlDialectBuilder;
import org.datagear.util.test.DBTestSupport;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 服务实现类单元测试支持类。
 * 
 * @author datagear@163.com
 *
 */
public class ServiceImplTestSupport extends DBTestSupport
{
	private static final DataSource DATA_SOURCE;

	static
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dataSource.setUrl("jdbc:derby:target/test/derby;create=true");

		DbVersionManager bean = new DbVersionManager(dataSource);
		bean.upgrade();

		DATA_SOURCE = dataSource;
	}

	private final SqlSessionFactory sqlSessionFactory;

	private final MbSqlDialect dialect;

	public ServiceImplTestSupport()
	{
		super();

		try
		{
			PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resourcePatternResolver
					.getResources("classpath*:org/datagear/management/mapper/*.xml");

			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
			bean.setDataSource(getDataSource());
			bean.setMapperLocations(resources);

			this.sqlSessionFactory = bean.getObject();

			this.dialect = new MbSqlDialectBuilder().build(getDataSource());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public SqlSessionFactory getSqlSessionFactory()
	{
		return sqlSessionFactory;
	}

	public MbSqlDialect getDialect()
	{
		return dialect;
	}

	@Override
	protected Connection getConnection() throws SQLException
	{
		return DATA_SOURCE.getConnection();
	}

	@Override
	protected DataSource getDataSource() throws SQLException
	{
		return DATA_SOURCE;
	}
}
