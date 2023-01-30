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

package org.datagear.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 事务配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
@ImportResource("classpath:org/datagear/web/transactionConfig.xml")
public class TransactionConfig
{
	/** transactionConfig.xml中使用此Bean名 */
	public static final String TX_MANAGER_BEAN = "txManager";

	private DataSourceConfig dataSourceConfig;

	@Autowired
	public TransactionConfig(DataSourceConfig dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	public DataSourceConfig getDataSourceConfig()
	{
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfig dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	@Bean(TX_MANAGER_BEAN)
	public PlatformTransactionManager txManager()
	{
		PlatformTransactionManager bean = new DataSourceTransactionManager(this.dataSourceConfig.dataSource());
		return bean;
	}
}
