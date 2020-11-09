/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
@ImportResource("org/datagear/web/transactionConfig.xml")
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
