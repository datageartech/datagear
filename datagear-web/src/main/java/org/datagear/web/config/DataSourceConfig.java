/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
	@ConfigurationProperties(prefix = "datasource")
	public DataSource dataSource()
	{
		// connection模块使用了DPCP2，所以这里也统一采用，避免多引入其他连接池库
		return new BasicDataSource();
	}
}
