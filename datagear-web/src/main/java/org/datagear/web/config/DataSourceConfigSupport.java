/*
 * Copyright 2018-2024 datagear.tech
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
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * <pre>
 * {@code @Configuration}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 */
@Configuration
public class DataSourceConfigSupport
{
	private Environment environment;

	@Autowired
	public DataSourceConfigSupport(Environment environment)
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
