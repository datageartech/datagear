/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 数据源配置。
 * 
 * @author datagear@163.com
 */
@Configuration
public class DataSourceConfig
{
	private Environment environment;

	@Autowired
	public DataSourceConfig(Environment environment)
	{
		super();
		this.environment = environment;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Bean
	public DataSource dataSource()
	{
		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setDriverClassName(org.apache.derby.jdbc.EmbeddedDriver.class.getName());
		basicDataSource.setUrl(buildDerbyURL());

		return basicDataSource;
	}

	protected String buildDerbyURL()
	{
		return "jdbc:derby:" + getDerbyAbsolutePath() + ";create=true";
	}

	protected String getDerbyAbsolutePath()
	{
		return this.environment.getProperty("directory.derby");
	}
}
