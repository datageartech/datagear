/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.impl;

import org.apache.ibatis.session.SqlSessionFactory;
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
	private SqlSessionFactory sqlSessionFactory;

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
}
